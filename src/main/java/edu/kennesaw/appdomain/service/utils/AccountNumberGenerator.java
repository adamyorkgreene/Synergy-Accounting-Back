package edu.kennesaw.appdomain.service.utils;

import edu.kennesaw.appdomain.types.AccountCategory;

import java.util.Map;

public class AccountNumberGenerator {

    private static final Map<AccountCategory, Long[]> ACCOUNT_NUMBER_RANGES = Map.of(
            AccountCategory.ASSET, new Long[]{1000L, 1999L},
            AccountCategory.LIABILITY, new Long[]{2000L, 2999L},
            AccountCategory.EQUITY, new Long[]{3000L, 3999L},
            AccountCategory.REVENUE, new Long[]{4000L, 4999L},
            AccountCategory.EXPENSE, new Long[]{5000L, 5999L}
    );

    public static Long[] getRange(AccountCategory category) {
        return ACCOUNT_NUMBER_RANGES.get(category);
    }
}