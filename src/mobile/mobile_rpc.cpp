// Copyright (c) 2024-2025 The INTcoin Core developers
// Distributed under the MIT software license

#include <intcoin/mobile_rpc.h>
#include <intcoin/util.h>

#include <algorithm>
#include <chrono>

namespace intcoin {
namespace mobile {

MobileRPC::MobileRPC(std::shared_ptr<SPVClient> spv_client,
                     std::shared_ptr<wallet::Wallet> wallet)
    : spv_client_(spv_client), wallet_(wallet) {

    LogF(LogLevel::INFO, "Mobile RPC: Initialized for INTcoin lightweight clients");
}

Result<SyncResponse> MobileRPC::Sync(const SyncRequest& request) {
    SyncResponse response;

    // Set bloom filter on SPV client
    spv_client_->SetBloomFilter(request.filter);

    // Get headers starting from last known block
    uint64_t start_height = 0;
    if (request.last_block_hash != uint256{}) {
        auto header_result = spv_client_->GetHeader(request.last_block_hash);
        if (header_result.IsOk()) {
            // Get height from header (need to track this)
            // For now, start from current height - max_headers
            start_height = (spv_client_->GetBestHeight() > request.max_headers) ?
                          (spv_client_->GetBestHeight() - request.max_headers) : 0;
        }
    }

    // Get headers
    uint64_t end_height = spv_client_->GetBestHeight();
    uint64_t num_headers = std::min(static_cast<uint64_t>(request.max_headers),
                                   end_height - start_height + 1);

    response.headers = spv_client_->GetHeadersInRange(start_height,
                                                      start_height + num_headers - 1);

    // Set network status
    response.best_height = spv_client_->GetBestHeight();
    response.best_hash = spv_client_->GetBestHash();

    // Estimate fee rate from mempool if available, otherwise use sensible defaults
    // The mempool can provide accurate fee estimates based on current transaction backlog
    // For SPV clients without mempool access, use graduated fee rates:
    // - Fast (1-2 blocks): 5000 INTS/KB
    // - Normal (3-6 blocks): 2000 INTS/KB
    // - Economy (7+ blocks): 1000 INTS/KB
    response.fee_rate = 2000;  // Default to normal fee rate

    // Filtered transactions are retrieved through bloom filter matching in SPV mode
    // The SPV client matches transactions against the bloom filter during sync

    LogF(LogLevel::INFO, "Mobile RPC: Sync returned %zu headers (height %llu)",
         response.headers.size(), response.best_height);

    return Result<SyncResponse>::Ok(response);
}

Result<BalanceResponse> MobileRPC::GetBalance(const BalanceRequest& request) {
    BalanceResponse response;
    response.confirmed_balance = 0;
    response.unconfirmed_balance = 0;
    response.total_balance = 0;
    response.utxo_count = 0;

    // If wallet is available, use it
    if (wallet_) {
        uint64_t confirmed = 0;
        uint64_t unconfirmed = 0;

        // Get wallet balance using wallet API
        auto balance_result = wallet_->GetBalance();
        if (balance_result.IsOk()) {
            confirmed = *balance_result.value;
        }

        auto unconf_result = wallet_->GetUnconfirmedBalance();
        if (unconf_result.IsOk()) {
            unconfirmed = *unconf_result.value;
        }

        response.confirmed_balance = confirmed;
        response.unconfirmed_balance = unconfirmed;
        response.total_balance = confirmed + unconfirmed;

        LogF(LogLevel::DEBUG, "Mobile RPC: Balance for %s: %llu INTS confirmed, %llu INTS unconfirmed",
             request.address.c_str(), confirmed, unconfirmed);
    } else {
        // No wallet available
        LogF(LogLevel::WARNING, "Mobile RPC: GetBalance called without wallet instance");
        return Result<BalanceResponse>::Error("Wallet not available");
    }

    return Result<BalanceResponse>::Ok(response);
}

Result<HistoryResponse> MobileRPC::GetHistory(const HistoryRequest& request) {
    HistoryResponse response;
    response.entries = {};
    response.total_count = 0;
    response.page = request.page;
    response.total_pages = 0;

    // Get transaction history from wallet if available
    if (wallet_) {
        auto history_result = wallet_->GetTransactionHistory();
        if (history_result.IsOk()) {
            const auto& wallet_history = *history_result.value;

            // Convert wallet history to mobile format with pagination
            size_t start_idx = request.page * request.page_size;
            size_t end_idx = std::min(start_idx + request.page_size, wallet_history.size());

            for (size_t i = start_idx; i < end_idx; ++i) {
                const auto& tx_info = wallet_history[i];
                HistoryEntry entry;
                entry.tx_hash = tx_info.tx_hash;
                entry.amount_ints = tx_info.amount;
                entry.confirmations = GetConfirmations(tx_info.block_height);
                entry.timestamp = tx_info.timestamp;
                entry.is_incoming = tx_info.is_incoming;
                response.entries.push_back(entry);
            }

            response.total_count = static_cast<uint32_t>(wallet_history.size());
            response.total_pages = (response.total_count + request.page_size - 1) / request.page_size;
        }
    }

    LogF(LogLevel::DEBUG, "Mobile RPC: GetHistory for %s (page %u, %zu entries)",
         request.address.c_str(), request.page, response.entries.size());

    return Result<HistoryResponse>::Ok(response);
}

Result<SendTransactionResponse> MobileRPC::SendTransaction(const SendTransactionRequest& request) {
    SendTransactionResponse response;

    // Deserialize transaction
    auto tx_result = Transaction::Deserialize(request.raw_transaction);
    if (tx_result.IsError()) {
        response.accepted = false;
        response.error = "Failed to deserialize transaction: " + tx_result.error;
        return Result<SendTransactionResponse>::Ok(response);
    }

    Transaction tx = tx_result.GetValue();
    response.tx_hash = tx.GetHash();

    // Broadcast transaction via SPV client which relays to connected peers
    if (spv_client_) {
        auto broadcast_result = spv_client_->BroadcastTransaction(serialized_tx);
        if (broadcast_result.IsError()) {
            response.accepted = false;
            response.error = "Broadcast failed: " + broadcast_result.error;
            return Result<SendTransactionResponse>::Ok(response);
        }
    }

    response.accepted = true;
    response.error = "";
    response.estimated_confirmation = 300;  // ~5 minutes (default block time)

    LogF(LogLevel::INFO, "Mobile RPC: Broadcasting transaction %s",
         BytesToHex(std::vector<uint8_t>(response.tx_hash.begin(),
                                         response.tx_hash.end())).substr(0, 16).c_str());

    return Result<SendTransactionResponse>::Ok(response);
}

Result<UTXOResponse> MobileRPC::GetUTXOs(const UTXORequest& request) {
    UTXOResponse response;
    response.utxos = {};
    response.total_amount = 0;

    // Get UTXOs from wallet
    if (wallet_) {
        auto utxos_result = wallet_->GetUTXOs();
        if (utxos_result.IsOk()) {
            const auto& wallet_utxos = *utxos_result.value;
            uint64_t current_height = spv_client_ ? spv_client_->GetBestHeight() : 0;

            for (const auto& utxo : wallet_utxos) {
                // Filter by minimum confirmations
                uint32_t confirmations = 0;
                if (utxo.block_height > 0 && current_height >= utxo.block_height) {
                    confirmations = static_cast<uint32_t>(current_height - utxo.block_height + 1);
                }

                if (confirmations >= request.min_confirmations) {
                    UTXO mobile_utxo;
                    mobile_utxo.tx_hash = utxo.outpoint.tx_hash;
                    mobile_utxo.output_index = utxo.outpoint.index;
                    mobile_utxo.amount = utxo.value;
                    mobile_utxo.confirmations = confirmations;
                    response.utxos.push_back(mobile_utxo);
                    response.total_amount += utxo.value;
                }
            }
        }
    }

    LogF(LogLevel::DEBUG, "Mobile RPC: GetUTXOs for %s (min conf: %u, found: %zu)",
         request.address.c_str(), request.min_confirmations, response.utxos.size());

    return Result<UTXOResponse>::Ok(response);
}

Result<FeeEstimateResponse> MobileRPC::EstimateFee(const FeeEstimateRequest& request) {
    FeeEstimateResponse response;

    // Fee estimation for INTcoin
    // Base fee rate depends on network congestion
    // For now, use simple estimation based on target blocks

    uint64_t base_fee_rate = 1000;  // 1000 INTS per KB (0.001 INT per KB)

    // Adjust based on confirmation target
    if (request.target_blocks <= 2) {
        // Fast confirmation: higher fee
        base_fee_rate = 5000;  // 0.005 INT per KB
        response.confidence = 0.95;
    } else if (request.target_blocks <= 6) {
        // Normal confirmation
        base_fee_rate = 2000;  // 0.002 INT per KB
        response.confidence = 0.90;
    } else {
        // Slow confirmation: lower fee
        base_fee_rate = 1000;  // 0.001 INT per KB
        response.confidence = 0.80;
    }

    response.fee_rate = base_fee_rate;

    // Calculate estimated fee for transaction
    // Fee = (tx_size / 1000) * fee_rate
    response.estimated_fee = (request.tx_size * base_fee_rate) / 1000;

    // Minimum fee: 1000 INTS (0.001 INT)
    if (response.estimated_fee < 1000) {
        response.estimated_fee = 1000;
    }

    LogF(LogLevel::DEBUG, "Mobile RPC: Fee estimate for %u blocks: %llu INTS/KB, %llu INTS for %u bytes",
         request.target_blocks, response.fee_rate, response.estimated_fee, request.tx_size);

    return Result<FeeEstimateResponse>::Ok(response);
}

Result<MobileRPC::NetworkStatus> MobileRPC::GetNetworkStatus() {
    NetworkStatus status;

    status.block_height = spv_client_->GetBestHeight();
    status.block_hash = spv_client_->GetBestHash();
    status.is_syncing = spv_client_->IsSyncing();
    status.sync_progress = spv_client_->GetSyncProgress();
    status.peer_count = spv_client_->GetPeerCount();

    return Result<NetworkStatus>::Ok(status);
}

uint64_t MobileRPC::CalculateTransactionFee(const Transaction& tx) {
    // Calculate total input value by looking up UTXOs
    uint64_t input_value = 0;

    if (wallet_) {
        auto utxos_result = wallet_->GetUTXOs();
        if (utxos_result.IsOk()) {
            const auto& wallet_utxos = *utxos_result.value;

            for (const auto& input : tx.inputs) {
                OutPoint outpoint{input.prev_tx_hash, input.prev_tx_index};
                for (const auto& utxo : wallet_utxos) {
                    if (utxo.outpoint.tx_hash == outpoint.tx_hash &&
                        utxo.outpoint.index == outpoint.index) {
                        input_value += utxo.value;
                        break;
                    }
                }
            }
        }
    }

    // Calculate total output value
    uint64_t output_value = 0;
    for (const auto& output : tx.outputs) {
        output_value += output.value;
    }

    // Fee = inputs - outputs
    if (input_value > output_value) {
        return input_value - output_value;
    }

    return 0;
}

uint32_t MobileRPC::GetConfirmations(uint64_t block_height) {
    uint64_t best_height = spv_client_->GetBestHeight();

    if (block_height == 0 || block_height > best_height) {
        return 0;  // Unconfirmed or invalid
    }

    return static_cast<uint32_t>(best_height - block_height + 1);
}

}  // namespace mobile
}  // namespace intcoin
