# See docs/installing.md

build: localnet haveno

clean:
	./gradlew clean

#clean-localnet:
#	rm -rf .localnet

localnet:
	mkdir -p .localnet

#nodes: localnet
#	./scripts/haveno_deps.sh

penumbra:
	./gradlew build

penumbra-apps: # quick build desktop and daemon apps without tests, etc
	./gradlew :core:compileJava :desktop:build

deploy:
	# create a new screen session named 'localnet'
	screen -dmS localnet
	# deploy each node in its own named screen window
	for target in \
		seednode-local \
		user1-desktop-local \
		user2-desktop-local \
		arbitrator-desktop-local; do \
			screen -S localnet -X screen -t $$target; \
			screen -S localnet -p $$target -X stuff "make $$target\n"; \
		done;
	# give bitcoind rpc server time to start
	sleep 5

deploy-local:
	# create a new screen session named 'localnet'
	screen -dmS localnet
	# deploy each node in its own named screen window
	for target in \
		seednode-local \
		alice-desktop-local \
		bob-desktop-local \
		arbitrator-desktop-local; do \
			screen -S localnet -X screen -t $$target; \
			screen -S localnet -p $$target -X stuff "make $$target\n"; \
		done;
	# give bitcoind rpc server time to start
	sleep 5

seednode:
	./haveno-seednode \
		--baseCurrencyNetwork=XMR_MAINNET \
		--useLocalhostForP2P=false \
		--useDevPrivilegeKeys=false \
		--seedNodes=5keoulw5wjxpujo5egboikc6bfmdtqooidx7ead3hwd3wk4ofairfxqd.onion:2002 \
		--nodePort=2002 \
		--appName=penumbra-seed \

seednode-local:
	./haveno-seednode \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=true \
		--useDevPrivilegeKeys=true \
		--nodePort=2002 \
		--appName=haveno-XMR_LOCAL_Seed_2002 \


arbitrator-desktop:
	# Arbitrator and mediator need to be registerd in the UI after launching it.
	./haveno-desktop \
		--baseCurrencyNetwork=XMR_MAINNET \
		--useLocalhostForP2P=false \
		--useDevPrivilegeKeys=false \
		--seedNodes=5keoulw5wjxpujo5egboikc6bfmdtqooidx7ead3hwd3wk4ofairfxqd.onion:2002 \
		--nodePort=4444 \
		--appName=penumbra-arbitrator \
		--apiPassword=apitest \
		--apiPort=9998

#arbitrator-desktop-local:
#	# Arbitrator and mediator need to be registerd in the UI after launching it.
#	./haveno-desktop \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=true \
		--useDevPrivilegeKeys=true \
		--nodePort=4444 \
		--appName=haveno-XMR_LOCAL_arbitrator \
		--apiPassword=apitest \
		--apiPort=9998 \
		--passwordRequired=false


arbitrator-daemon:
	# Arbitrator and mediator need to be registerd in the UI before launching the daemon.
	./haveno-daemon \
		--baseCurrencyNetwork=XMR_MAINNET \
		--useLocalhostForP2P=false \
		--useDevPrivilegeKeys=false \
		--seedNodes=5keoulw5wjxpujo5egboikc6bfmdtqooidx7ead3hwd3wk4ofairfxqd.onion:2002 \
		--nodePort=4444 \
		--appName=penumbra-arbitrator \
		--apiPassword=apitest \
		--apiPort=9998

arbitrator-daemon-local:
	# Arbitrator and mediator need to be registerd in the UI before launching the daemon.
	./haveno-daemon \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=true \
		--useDevPrivilegeKeys=true \
		--nodePort=4444 \
		--appName=haveno-XMR_LOCAL_arbitrator \
		--apiPassword=apitest \
		--apiPort=9998


alice-desktop:
	./haveno-desktop \
		--baseCurrencyNetwork=XMR_MAINNET \
        --useLocalhostForP2P=false \
        --useDevPrivilegeKeys=false \
        --seedNodes=5keoulw5wjxpujo5egboikc6bfmdtqooidx7ead3hwd3wk4ofairfxqd.onion:2002 \
		--nodePort=5555 \
		--appName=penumbra-alice \
		--apiPassword=apitest \
		--apiPort=9999 \
		--walletRpcBindPort=38091

alice-desktop-local:
	./haveno-desktop \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=true \
		--useDevPrivilegeKeys=true \
		--nodePort=5555 \
		--appName=haveno-XMR_LOCAL_user1 \
		--apiPassword=apitest \
		--apiPort=9999 \
		--walletRpcBindPort=38091 \
		--passwordRequired=false

alice-daemon:
	./haveno-daemon \
		--baseCurrencyNetwork=XMR_MAINNET \
        --useLocalhostForP2P=false \
        --useDevPrivilegeKeys=false \
        --seedNodes=5keoulw5wjxpujo5egboikc6bfmdtqooidx7ead3hwd3wk4ofairfxqd.onion:2002 \
		--nodePort=5555 \
		--appName=penumbra-alice \
		--apiPassword=apitest \
		--apiPort=9999 \
		--walletRpcBindPort=38091

alice-daemon-local:
	./haveno-daemon \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=true \
		--useDevPrivilegeKeys=true \
		--nodePort=5555 \
		--appName=haveno-XMR_LOCAL_user1 \
		--apiPassword=apitest \
		--apiPort=9999 \
		--walletRpcBindPort=38091


bob-desktop:
	./haveno-desktop \
		--baseCurrencyNetwork=XMR_MAINNET \
        --useLocalhostForP2P=false \
        --useDevPrivilegeKeys=false \
        --seedNodes=5keoulw5wjxpujo5egboikc6bfmdtqooidx7ead3hwd3wk4ofairfxqd.onion:2002 \
		--nodePort=6666 \
		--appName=penumbra-bob \
		--apiPassword=apitest \
		--apiPort=10000 \
		--walletRpcBindPort=38092

bob-desktop-local:
	./haveno-desktop \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=true \
		--useDevPrivilegeKeys=true \
		--nodePort=6666 \
		--appName=haveno-XMR_LOCAL_user2 \
		--apiPassword=apitest \
		--apiPort=10000 \
		--walletRpcBindPort=38092

user2-daemon-local:
	./haveno-daemon \
		--baseCurrencyNetwork=XMR_MAINNET \
        --useLocalhostForP2P=false \
        --useDevPrivilegeKeys=false \
        --seedNodes=5keoulw5wjxpujo5egboikc6bfmdtqooidx7ead3hwd3wk4ofairfxqd.onion:2002 \
		--nodePort=6666 \
		--appName=penumbra-bob \
		--apiPassword=apitest \
		--apiPort=10000 \
		--walletRpcBindPort=38092

bob-daemon-local:
	./haveno-daemon \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=true \
		--useDevPrivilegeKeys=true \
		--nodePort=6666 \
		--appName=haveno-XMR_LOCAL_user2 \
		--apiPassword=apitest \
		--apiPort=10000 \
		--walletRpcBindPort=38092 \
		--passwordRequired=false

#monero-shared:
#	./.localnet/monerod \
		--stagenet \
		--no-igd \
		--hide-my-port \
		--data-dir .localnet/stagenet \
		--add-exclusive-node 136.244.105.131:38080 \
		--rpc-login superuser:abctesting123 \
		--rpc-access-control-origins http://localhost:8080 \
		--fixed-difficulty 100

#monero-private1:
#	./.localnet/monerod \
		--stagenet \
		--bootstrap-daemon-address auto \
		--rpc-access-control-origins http://localhost:8080 \

#monero-private2:
#	./.localnet/monerod \
		--stagenet \
		--no-igd \
		--hide-my-port \
		--data-dir .localnet/stagenet/node2 \
		--p2p-bind-ip 127.0.0.1 \
		--rpc-bind-ip 0.0.0.0 \
		--confirm-external-bind \
		--add-exclusive-node 127.0.0.1:48080 \
		--rpc-login superuser:abctesting123 \
		--rpc-access-control-origins http://localhost:8080 \
		--fixed-difficulty 100

#funding-wallet:
#	./.localnet/monero-wallet-rpc \
		--stagenet \
		--daemon-address http://localhost:38081 \
		--daemon-login superuser:abctesting123 \
		--rpc-bind-port 38084 \
		--rpc-login rpc_user:abc123 \
		--rpc-access-control-origins http://localhost:8080 \
		--wallet-dir ./.localnet


user1-daemon-stagenet:
	./haveno-daemon \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=false \
		--useDevPrivilegeKeys=false \
		--nodePort=5555 \
		--appName=haveno-XMR_STAGENET_user1 \
		--apiPassword=apitest \
		--apiPort=9999 \
		--walletRpcBindPort=38091 \
		--passwordRequired=false

user1-desktop-stagenet:
	./haveno-desktop \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=false \
		--useDevPrivilegeKeys=false \
		--nodePort=5555 \
		--appName=haveno-XMR_STAGENET_user1 \
		--apiPassword=apitest \
		--apiPort=9999 \
		--walletRpcBindPort=38091

user2-daemon-stagenet:
	./haveno-daemon \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=false \
		--useDevPrivilegeKeys=false \
		--nodePort=6666 \
		--appName=haveno-XMR_STAGENET_user2 \
		--apiPassword=apitest \
		--apiPort=10000 \
		--walletRpcBindPort=38092 \
		--passwordRequired=false

user2-desktop-stagenet:
	./haveno-desktop \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=false \
		--useDevPrivilegeKeys=false \
		--nodePort=6666 \
		--appName=haveno-XMR_STAGENET_user2 \
		--apiPassword=apitest \
		--apiPort=10000 \
		--walletRpcBindPort=38092

user3-desktop-stagenet:
	./haveno-desktop \
		--baseCurrencyNetwork=XMR_STAGENET \
		--useLocalhostForP2P=false \
		--useDevPrivilegeKeys=false \
		--nodePort=8888 \
		--appName=haveno-XMR_STAGENET_user3 \
		--apiPassword=apitest \
		--apiPort=10002 \
		--walletRpcBindPort=38093