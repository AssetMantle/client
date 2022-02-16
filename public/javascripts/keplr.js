rpc = "http://localhost:26657";
lcd = "http://localhost:1317";
chainID = "test";
chainName = "AssetMantle Testnet";
keplrSet = false;
stakingDenom = "uatom";
maxGas = 250000;

async function initializeKeplr() {
    if (!window.keplr) {
        alert("Please install keplr extension");
    } else {
        if (!keplrSet) {
            await setChain();
            await window.keplr.enable(chainID);
            keplrSet = true;
        }
    }
}

initializeKeplr().catch(e => console.log(e));

async function getKeplrWallet() {
    if (!window.keplr) {
        alert("Please install keplr extension");
    } else {
        try {
            let offlineSigner = window.keplr.getOfflineSigner(chainID);
            let accounts = await offlineSigner.getAccounts();
            return [offlineSigner, accounts[0].address];
        } catch (e) {
            console.log(e)
        }
    }
}

async function Transaction(wallet, signerAddress, msgs, fee, memo = "") {
    const cosmJS = await window.SigningStargateClient.connectWithSigner(
        rpc,
        wallet,
    );
    return await cosmJS.signAndBroadcast(signerAddress, msgs, fee, memo);
}

async function signArbitrary(signer, data) {
    try {
        return await window.keplr.signArbitrary(chainID, signer, data);
    } catch (e) {
        console.log(e)
    }
}

function getTxFee(amount = 0, gas = maxGas) {
    return {amount: [{amount: String(amount), denom: stakingDenom}], gas: String(gas)};
}

async function setChain() {
    await window.keplr.experimentalSuggestChain({
        chainId: chainID,
        chainName: chainName,
        rpc: rpc,
        rest: lcd,
        bip44: {
            coinType: 118,
        },
        bech32Config: {
            bech32PrefixAccAddr: "cosmos",
            bech32PrefixAccPub: "cosmos" + "pub",
            bech32PrefixValAddr: "cosmos" + "valoper",
            bech32PrefixValPub: "cosmos" + "valoperpub",
            bech32PrefixConsAddr: "cosmos" + "valcons",
            bech32PrefixConsPub: "cosmos" + "valconspub",
        },
        currencies: [
            {
                coinDenom: "ATOM",
                coinMinimalDenom: "uatom",
                coinDecimals: 6,
                coinGeckoId: "cosmos",
            },
        ],
        feeCurrencies: [
            {
                coinDenom: "ATOM",
                coinMinimalDenom: "uatom",
                coinDecimals: 6,
                coinGeckoId: "cosmos",
            },
        ],
        stakeCurrency: {
            coinDenom: "ATOM",
            coinMinimalDenom: "uatom",
            coinDecimals: 6,
            coinGeckoId: "cosmos",
        },
        coinType: 118,
        gasPriceStep: {
            low: 0.01,
            average: 0.025,
            high: 0.03,
        },
    });

}