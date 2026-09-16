package com.jcaa.usersmanagement.infrastructure.adapter.persistence.mapper;

import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoAmount;
import com.jcaa.usersmanagement.domain.valueobject.GastoDate;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescription;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoOwner;
import com.jcaa.usersmanagement.domain.valueobject.GastoPlace;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.dto.GastoPersistenceDto;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.entity.GastoEntity;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Data Mapper bidireccional: Entity -> Dto -> Model y vuelta.
 *
 * <p>Aisla por completo al dominio de JDBC. El dominio no conoce
 * `java.sql.Date` ni `double`, y la base no conoce Value Objects.
 */
@Component
public class GastoPersistenceMapper {

  public GastoPersistenceDto toDto(final GastoEntity entity) {
    return new GastoPersistenceDto(
        entity.idGasto(),
        entity.fecha() == null ? null : entity.fecha().toLocalDate(),
        BigDecimal.valueOf(entity.valorTotalSinIva()),
        BigDecimal.valueOf(entity.ivaTotal()),
        BigDecimal.valueOf(entity.valorTotalConIva()),
        entity.nombreUsuario(),
        entity.lugar(),
        entity.descripcion());
  }

  public GastoModel toDomain(final GastoPersistenceDto dto) {
    return GastoModel.reconstitute(
        dto.idGasto() > 0 ? GastoId.of(dto.idGasto()) : GastoId.notAssigned(),
        GastoDate.of(dto.fecha()),
        GastoAmount.of(dto.valorTotalSinIva()),
        GastoAmount.of(dto.ivaTotal()),
        GastoAmount.of(dto.valorTotalConIva()),
        GastoOwner.of(dto.nombreUsuario()),
        GastoPlace.of(dto.lugar()),
        GastoDescription.of(dto.descripcion()));
  }

  public GastoModel toDomain(final GastoEntity entity) {
    return toDomain(toDto(entity));
  }

  public GastoPersistenceDto toDto(final GastoModel model) {
    return new GastoPersistenceDto(
        model.getId().value(),
        model.getFecha().value(),
        model.getValorTotalSinIva().value(),
        model.getIvaTotal().value(),
        model.getValorTotalConIva().value(),
        model.getNombreUsuario().value(),
        model.getLugar().value(),
        model.getDescripcion().isEmpty() ? null : model.getDescripcion().value());
  }
}
