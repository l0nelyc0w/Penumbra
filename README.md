

## Introducing Penumbra. A truly uncensorable DEX.
<p>
<img style="float: right;" src="https://gitlab.com/l0nelyc0w/penumbra/-/raw/penumbra-master/desktop/src/main/resources/images/logo_splash.png">
Penumbra is a <a href="https://github.com/haveno-dex/haveno">Haveno's</a> fork that is strongly aligned with the values of decentralisation, privacy and censorship resistance.
</p>

An illustration of how Penumbra will operate as a DEX can be found [here](https://gitlab.com/l0nelyc0w/penumbra/-/design_management/designs/431134/12effd1d1ee0bc5a8e50843b2c9b174d9d630164/resized_image/v432x230).

### Features
- **True** non-custodial trades. In contrast to Havenos 2/3 multisig, [Penumbra uses 2/2 multisig](https://gitlab.com/l0nelyc0w/penumbra/-/tree/penumbra-master/docs/new_protocol.pdf) without arbitration.
- No council and shadow entities. There are no hidden people that you have never heard about influencing this project and its decisions.
- No dev tax. Trade fees profit the respective operator on which the offer was posted instead.
- Operators can join the network. See "Requirements for Operators" (TBA)
- Strictly bounty driven development.

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

### Rules
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

People who want to learn about Penumbra, contribute donations, code or ideas or provide feedback can do so on the following channels:

### Matrix channels

General discussion: [#penumbra](https:/matrix.to/#/#penumbra:penumbra.social)

Development: [#penumbra-dev](https://matrix.to/#/#penumbra-dev:penumbra.social)

### Telegram

Unofficial community: https://t.me/PenumbraDEX

### Monero donation address

`42sjRNZYxcyWK3Bd3e6MNaR8zmjNrze8W5fDjttJ152WPReFUj5ung4fw7y73DTtFXjVRGSkonjW5J5XvUXub2xEV3ufoK4`

![monero donation address](qrcode.jpeg)

### BIP47 Paynym payment code
`PM8TJcRbSssbcnkNusjQdDYvVMtAFj717B9GvSGBdyJqrzVrPUWf2VGZ1skNBTzeSFE8Y2MMkHyLHsbdmGXbJd46dEwcTB1UbPckMFynYQw8zgdJrvoC`

<img src="https://paynym.is/PM8TJcRbSssbcnkNusjQdDYvVMtAFj717B9GvSGBdyJqrzVrPUWf2VGZ1skNBTzeSFE8Y2MMkHyLHsbdmGXbJd46dEwcTB1UbPckMFynYQw8zgdJrvoC/codeimage" height="185" width="185">

---
## I want to donate Bitcoin how can I do so?

You can do so in two ways:
1. If your wallet supports[^1] it, you can use our [Paynym](https://paynym.is/+autumnwaterfall0F5). (Learn [more](https://bitcoiner.guide/paynym/) about Paynyms)
2. Use a swap service[^2] like [Majestic Bank](https://majesticbank.is/) or [FixedFloat](https://fixedfloat.com) to pay with Bitcoin to the projects Monero address.

[^1]: wallet support: [Samourai](https://samouraiwallet.com/), [Sparrow](https://sparrowwallet.com)

[^2]: [more](https://kycnot.me) swap services
