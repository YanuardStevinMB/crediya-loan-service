package com.crediya.loan.model.shared.pagination;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class ApplicationFilter {
    private final Set<Long> stateIds;
    private final Set<String> stateCodes;
    private final Set<Long> loanTypeIds;
    private final String loanTypeName;

    private final String search;            // email / nombre / tipo_prestamo.nombre
    private final BigDecimal minAmount, maxAmount;
    private final LocalDate minTerm, maxTerm;

    public ApplicationFilter(Set<Long> stateIds, Set<String> stateCodes,
                             Set<Long> loanTypeIds, String loanTypeName,
                             String search, BigDecimal minAmount, BigDecimal maxAmount,
                             LocalDate minTerm, LocalDate maxTerm) {
        this.stateIds = stateIds; this.stateCodes = stateCodes;
        this.loanTypeIds = loanTypeIds; this.loanTypeName = loanTypeName;
        this.search = search; this.minAmount = minAmount; this.maxAmount = maxAmount;
        this.minTerm = minTerm; this.maxTerm = maxTerm;
    }
    public Set<Long> getStateIds() { return stateIds; }
    public Set<String> getStateCodes() { return stateCodes; }
    public Set<Long> getLoanTypeIds() { return loanTypeIds; }
    public String getLoanTypeName() { return loanTypeName; }
    public String getSearch() { return search; }
    public BigDecimal getMinAmount() { return minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public LocalDate getMinTerm() { return minTerm; }
    public LocalDate getMaxTerm() { return maxTerm; }
}
