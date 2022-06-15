## Introducing Penumbra. A truly uncensorable DEX.

Penumbra is a <a href="https://github.com/haveno-dex/haveno">Haveno's</a> fork that is strongly aligned with the values of decentralisation, privacy and censorship resistance.

Main differences from Haveno
- True non-custodial trades. In contrast to Havenos 2/3 multisig, [Penumbra uses 2/2 multisig](https://gitlab.com/l0nelyc0w/penumbra/-/tree/penumbra-master/docs/new_protocol.pdf) without arbitration in case "something goes wrong".
- No council and shadow entities. There are no hidden people that you have never heard about influencing this project and its decisions.

---
## How to connect

First download and compile project
```
# download source
git clone https://github.com/l0nelyc0w/haveno penumbra
cd penumbra

# build project
./gradlew build

# create required folders
make localnet
```
After that to run a client to connect
```
make alice-desktop
```
Note: To connect to PenumbraDEX you need a fully synced Monero daemon to be running locally.

---
## 💰️ Bounties 💰️

Penumbra uses a bounty driven system to increase development engagement.
In order to apply for a bounty, leave a comment expressing your interest on the corresponding issue.
Search for issues with [bounties](https://gitlab.com/l0nelyc0w/penumbra/-/issues/?sort=created_date&state=opened&label_name%5B%5D=Bounty%20%F0%9F%92%B0%EF%B8%8F&first_page_size=20)

Rules
1. Bounty must be unassigned
2. Progress should be reported in a weekly manner
3. Payment is only given for complete and working PR's after merging
4. If no progress is made for over 40 days, bounty will be reassigned
---
## Dev etiquette

- Join the [#penumbra-dev](https://matrix.to/#/#penumbra-dev:penumbra.social) matrix channel
- Squash commits before merging
- Do not break current code convention
- If in doubt, ask for help
- PR's won't be merged until at least 48 hours have passed from successful completion
---
## Made by the community for the community

People who want to contribute donations, code or ideas can do so on the following Matrix channels.

General: [#penumbra](https:/matrix.to/#/#penumbra:penumbra.social)

Development:[#penumbra-dev](https://matrix.to/#/#penumbra-dev:penumbra.social)

`42sjRNZYxcyWK3Bd3e6MNaR8zmjNrze8W5fDjttJ152WPReFUj5ung4fw7y73DTtFXjVRGSkonjW5J5XvUXub2xEV3ufoK4`

![monero donation address](qrcode.jpeg)

