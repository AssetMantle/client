msgSendTypeUrl = "/cosmos.bank.v1beta1.MsgSend";
msgDelegateTypeUrl = "/cosmos.staking.v1beta1.MsgDelegate";
msgRedelegateTypeUrl = "/cosmos.staking.v1beta1.MsgBeginRedelegate";
msgUnbondTypeUrl = "/cosmos.staking.v1beta1.MsgUndelegate";
msgWithdrawRewardsTypeUrl = "/cosmos.distribution.v1beta1.MsgWithdrawDelegatorReward";
msgSetWithdrawAddressTypeUrl = "/cosmos.distribution.v1beta1.MsgSetWithdrawAddress";
msgTransferTypeUrl = "/ibc.applications.transfer.v1.MsgTransfer";
msgValidatorCommission = '/cosmos.distribution.v1beta1.MsgWithdrawValidatorCommission';

function SendMsg(fromAddress, toAddress, amount, denom) {
    return {
        typeUrl: msgSendTypeUrl,
        value: window.msgSend.fromPartial({
            fromAddress: fromAddress,
            toAddress: toAddress,
            amount: [{
                denom: denom,
                amount: String(amount),
            }],
        }),
    };
}


function DelegateMsg(delegatorAddress, validatorAddress, amount, denom) {
    return {
        typeUrl: msgDelegateTypeUrl,
        value: window.msgDelegate.fromPartial({
                delegatorAddress: delegatorAddress,
                validatorAddress: validatorAddress,
                amount: {
                    denom: denom,
                    amount: String(amount),
                },
            }
        ),
    };
}

function RedelegateMsg(delegatorAddress, validatorSrcAddress, validatorDstAddress, amount, denom) {
    return {
        typeUrl: msgRedelegateTypeUrl,
        value: window.msgBeginRedelegate.fromPartial({
                delegatorAddress: delegatorAddress,
                validatorSrcAddress: validatorSrcAddress,
                validatorDstAddress: validatorDstAddress,
                amount: {
                    denom: denom,
                    amount: String(amount),
                },
            }
        ),
    };
}

function UnbondMsg(delegatorAddress, validatorAddress, amount, denom) {
    return {
        typeUrl: msgUnbondTypeUrl,
        value: window.msgUndelegate.fromPartial({
                delegatorAddress: delegatorAddress,
                validatorAddress: validatorAddress,
                amount: {
                    denom: denom,
                    amount: String(amount),
                },
            }
        ),
    };

}

function WithdrawMsg(delegatorAddress, validatorAddress) {
    return {
        typeUrl: msgWithdrawRewardsTypeUrl,
        value: window.msgWithdrawDelegatorReward.fromPartial({
            delegatorAddress: delegatorAddress,
            validatorAddress: validatorAddress,
        }),
    };
}

function SetWithDrawAddressMsg(delegatorAddress, withdrawAddress) {
    return {
        typeUrl: msgSetWithdrawAddressTypeUrl,
        value: window.msgSetWithdrawAddress.fromPartial({
            delegatorAddress: delegatorAddress,
            withdrawAddress: trimWhiteSpaces(withdrawAddress),
        }),
    };
}

function TransferMsg(channel, fromAddress, toAddress, amount, timeoutHeight, timeoutTimestamp, denom, port = "transfer") {
    return {
        typeUrl: msgTransferTypeUrl,
        value: window.msgTransfer.fromPartial({
            sourcePort: port,
            sourceChannel: channel,
            token: coin(stringToNumber(amount), denom),
            sender: trimWhiteSpaces(fromAddress),
            receiver: trimWhiteSpaces(toAddress),
            timeoutHeight: {
                revisionNumber: timeoutHeight.revisionNumber,
                revisionHeight: timeoutHeight.revisionHeight,
            },
            timeoutTimestamp: timeoutTimestamp,
        }),
    };
}

function ValidatorCommissionMsg(address) {
    return {
        typeUrl: msgValidatorCommission,
        value: window.msgWithdrawValidatorCommission.fromPartial({
            validatorAddress: address,
        }),
    };
}
