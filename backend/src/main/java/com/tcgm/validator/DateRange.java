package com.tcgm.validator;

import java.time.LocalDate;

public interface DateRange {
    LocalDate getStartDate();
    LocalDate getEndDate();
}