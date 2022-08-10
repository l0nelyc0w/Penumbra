#!/bin/bash


# Hashes and version of bitcoin core: https://bitcoin.org/bin/
#BTC_HASH_MAC="1ea5cedb64318e9868a66d3ab65de14516f9ada53143e460d50af428b5aec3c7"
#BTC_HASH_LINUX="366eb44a7a0aa5bd342deea215ec19a184a11f2ca22220304ebb20b9c8917e2b"
#BTC_VERSION=0.21.1

is_mac() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        return 0
    else
        return 1
    fi
}

is_linux() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        return 0
    else
        return 1
    fi
}

dw_source() {
    if command -v wget &> /dev/null; then
        downloader="wget"
    elif command -v curl &> /dev/null; then
        downloader="curl -L -O"
    else
        echo "! curl or wget are not installed. Please install one of the two"
        exit 1
    fi

    ${downloader} "$1"
}


# Verify hashes of bitcoind and bitcoin-cli
check_bitcoin() {
    if is_mac; then
        shasum -a 256 -c <<< ''"${BTC_HASH_MAC}"' *bitcoin-'"${BTC_VERSION}"'-'"${btc_platform}"'.tar.gz' || exit 1
    else
        echo "${BTC_HASH_LINUX} bitcoin-${BTC_VERSION}-${btc_platform}.tar.gz" | sha256sum -c || exit 1
    fi

    echo "-> Bitcoin binaries downloaded and verified"
}

# Download Monero bins
dw_monero() {
    pkill -9 monero-wallet-r
    extract_monero() {
        echo "-> extracting monerod and monero-wallet-rpc from archive" && \
        tar -xjf monero-linux-x64-v0.18.0.0.tar.bz2 && \
        cp monero-x86_64-linux-gnu-v0.18.0.0/monerod monero-x86_64-linux-gnu-v0.18.0.0/monero-wallet-rpc . && \
        chmod +x {monerod,monero-wallet-rpc} || exit 1
    }

    if is_mac; then
        platform="mac"
    else
        platform="linux"
    fi

    if [ -f monero-linux-x64-v0.18.0.0.tar.bz2 ]; then
        extract_monero
    else
        dw_source https://downloads.getmonero.org/cli/monero-linux-x64-v0.18.0.0.tar.bz2 || { echo "! something went wrong while downloading the Monero binaries. Exiting...";  exit 1; } && \
        extract_monero
    fi
}

# Download Bitcoin bins
dw_bitcoin() {
    if is_mac; then
        btc_platform="osx64"
    else
        btc_platform="x86_64-linux-gnu"
    fi

    if [ -f bitcoin-${BTC_VERSION}-${btc_platform}.tar.gz ]; then
        check_bitcoin
    else
        dw_source https://bitcoin.org/bin/bitcoin-core-${BTC_VERSION}/bitcoin-${BTC_VERSION}-${btc_platform}.tar.gz || { echo "! something went wrong while downloading the Bitcoin binaries. Exiting..."; exit 1; } && \
        check_bitcoin
    fi

    tar -xzf bitcoin-${BTC_VERSION}-${btc_platform}.tar.gz && \
    cp bitcoin-${BTC_VERSION}/bin/{bitcoin-cli,bitcoind} . && \
    rm -r bitcoin-${BTC_VERSION} || exit 1
}

while true; do
    cd .localnet

    if ! is_linux && ! is_mac; then
        bins_deps=("monerod" "monero-wallet-rpc") # "bitcoind" "bitcoin-cli"

        for i in ${bins_deps[@]}; do
            [ -f "$i" ] || { echo "${i} not found."; echo "Dependencies are installed automatically only on Linux and Mac. Please manually install bitcoind, bitcoin-cli, monerod, and monero-wallet-rpc executables into haveno/.localnet/ before running make."; exit 1; }
        done
        exit 0
    fi

    dw_monero
    # dw_bitcoin
    exit 0
done
