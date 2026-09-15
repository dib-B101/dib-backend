package com.b101.dib.member.query.dto;

import com.b101.dib.common.util.AccountMasker;
import com.b101.dib.member.domain.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SettlementAccountDetailDto {
    private String bankName;
    private String maskedAccountNumber;
    private String accountHolder;

    public static SettlementAccountDetailDto from(Member m) {
        SettlementAccountDetailDto dto = new SettlementAccountDetailDto();
        dto.setBankName(m.getBankName());
        dto.setMaskedAccountNumber(AccountMasker.mask(m.getAccountNumber()));
        dto.setAccountHolder(m.getAccountHolder());
        return dto;
    }
}
