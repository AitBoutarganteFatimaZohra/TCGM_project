package com.tcgm.mapper;

import com.tcgm.dto.response.DashboardStatsResponse;
import com.tcgm.dto.response.SiteStatsResponse;
import com.tcgm.dto.response.OuvrierStatsResponse;
import com.tcgm.dto.response.StatistiquesResponse;
import com.tcgm.model.Site;
import com.tcgm.model.Ouvrier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StatistiqueMapper {

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    @Mapping(target = "siteName", source = "name")
    @Mapping(target = "siteStatus", source = "status")
    SiteStatsResponse toSiteStatsResponse(Site site);

    @Mapping(target = "ouvrierName", expression = "java(ouvrier.getFirstName() + \" \" + ouvrier.getLastName())")
    OuvrierStatsResponse toOuvrierStatsResponse(Ouvrier ouvrier);

    // =========================================================
    // LISTES
    // =========================================================

    List<SiteStatsResponse> toSiteStatsResponseList(List<Site> sites);
    List<OuvrierStatsResponse> toOuvrierStatsResponseList(List<Ouvrier> ouvriers);
}