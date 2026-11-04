package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.query.GastoFilterQuery;
import com.jcaa.usersmanagement.application.service.dto.response.report.GastoReportResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.IvaAuditRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.MonthlyEvolutionRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.OwnerConsolidationRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.PlaceRankingRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.TopGastoRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.report.UserRoleRowResponse;
import com.jcaa.usersmanagement.application.service.dto.response.GastoResponse;

/**
 * Puerto de entrada del lado Query: proyecciones analiticas de solo lectura.
 *
 * <p>Cada metodo recibe su propio filtro. Dos reportes distintos nunca
 * comparten criterios, de modo que afinar uno no altera los demas.
 */
public interface GetGastoReportsUseCase {

  GastoReportResponse<IvaAuditRowResponse> ivaAudit(GastoFilterQuery query);

  GastoReportResponse<PlaceRankingRowResponse> placeRanking(GastoFilterQuery query);

  GastoReportResponse<OwnerConsolidationRowResponse> ownerConsolidation(GastoFilterQuery query);

  GastoReportResponse<MonthlyEvolutionRowResponse> monthlyEvolution(GastoFilterQuery query);

  GastoReportResponse<TopGastoRowResponse> topGastos(GastoFilterQuery query);

  GastoReportResponse<GastoResponse> transactionDetail(GastoFilterQuery query);

  GastoReportResponse<UserRoleRowResponse> userDirectory(GastoFilterQuery query);
}
