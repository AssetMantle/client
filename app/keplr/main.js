const stargate = require("@cosmjs/stargate");
const distributionTypes = require("cosmjs-types/cosmos/distribution/v1beta1/tx");
const ibcTypes = require("cosmjs-types/ibc/applications/transfer/v1/tx");
const bankTypes = require("cosmjs-types/cosmos/bank/v1beta1/tx");
const stakingTypes = require("cosmjs-types/cosmos/staking/v1beta1/tx");
const launchpad = require("@cosmjs/launchpad");

global.window.msgUndelegate = stakingTypes.MsgUndelegate;
global.window.msgDelegate = stakingTypes.MsgDelegate;
global.window.msgBeginRedelegate = stakingTypes.MsgBeginRedelegate;
global.window.msgSend = bankTypes.MsgSend;
global.window.msgTransfer = ibcTypes.MsgTransfer;
global.window.msgWithdrawValidatorCommission = distributionTypes.MsgWithdrawValidatorCommission;
global.window.msgWithdrawDelegatorReward = distributionTypes.MsgWithdrawDelegatorReward;
global.window.msgSetWithdrawAddress = distributionTypes.MsgSetWithdrawAddress;
global.window.SetupIbcExtension = stargate.setupIbcExtension;
global.window.QueryClient = stargate.QueryClient;
global.window.SigningCosmosClient = launchpad.SigningCosmosClient;
global.window.SigningStargateClient = stargate.SigningStargateClient;

JSON.stringify()