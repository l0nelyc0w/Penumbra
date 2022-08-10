#!/bin/bash

# Hashes and tag of our Monero testing binaries at https://github.com/haveno-dex/monero/releases
MONERO_HASH_MAC="9188d0ee6111c5f68da0002bbbfc3ecf1ad4c053e99495b17652e2b6bc15ef49"
MONERO_HASH_LINUX="ac5b335bbb5ee82e64d13898b951b8a3e1a9bd39b0dfbc3b08ea6be0d16d82f1"
MONERO_TAG="testing6"
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

# Verify Monero hash
check_monero() {
    #if is_mac; then
    #    shasum -a 256 -c <<<'4184a141be8ea31af9fd8aaf1b2e38737dafb76838f7d3527b0140942f1bbe87 *monero-linux-x64-v0.17.3.0.tar.bz2' || exit 1
    #else
        echo "ac18ce3d1189410a5c175984827d5d601974733303411f6142296d647f6582ce monero-linux-x64-v0.17.3.0.tar.bz2" | sha256sum -c || exit 1
    #fi

    echo "-> Monero binaries downloaded and verified"
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

    extract_monero() {
        echo "-> extracting monerod and monero-wallet-rpc from archive" && \
        tar -xzf "monero-bins-haveno-${platform}.tar.gz" && \
        chmod +x {monerod,monero-wallet-rpc} || exit 1
    }

    if is_mac; then
        platform="mac"
    else
        platform="linux"
    fi

    if [ -f monero-linux-x64-v0.17.3.0.tar.bz2 ]; then
        check_monero
    else
        dw_source https://downloads.getmonero.org/cli/monero-linux-x64-v0.17.3.0.tar.bz2 || { echo "! something went wrong while downloading the Monero binaries. Exiting...";  exit 1; } && \
        check_monero
    fi

    tar -xzf https://downloads.getmonero.org/cli/monero-linux-x64-v0.17.3.0.tar.bz2 && \
    chmod +x {monerod,monero-wallet-rpc} || exit 1
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
