package com.jcaa.usersmanagement.infrastructure.adapter.persistence.repository;

import com.jcaa.usersmanagement.application.port.out.DeleteGastoPort;
import com.jcaa.usersmanagement.application.port.out.GetAllGastosPort;
import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.port.out.SaveGastoPort;
import com.jcaa.usersmanagement.application.port.out.UpdateGastoPort;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.entity.GastoEntity;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception.GastoPersistenceException;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.mapper.GastoPersistenceMapper;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Adaptador secundario de persistencia para Gasto sobre MySQL.
 *
 * <p>Opera contra la tabla `gastos` ya existente, respetando sus nombres de
 * columna y su clave foranea `nombre_usuario -> usuarios(id)`. Toda sentencia
 * es parametrizada: no se concatena entrada de usuario en el SQL, lo que
 * elimina por construccion la inyeccion SQL (OWASP A03).
 *
 * <p>El adaptador nunca ejecuta DDL. La base es compartida con otros
 * ejercicios del curso y alterar su esquema afectaria a terceros.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class GastoRepositoryMySQL
    implements SaveGastoPort, UpdateGastoPort, GetGastoByIdPort, GetAllGastosPort, DeleteGastoPort {

  private static final String COLUMNS =
      "id_gasto, fecha, valor_total_sin_iva, iva_total, valor_total_con_iva, "
          + "nombre_usuario, lugar, descripcion";

  private static final String SQL_INSERT =
      "INSERT INTO gastos (fecha, valor_total_sin_iva, iva_total, valor_total_con_iva, "
          + "nombre_usuario, lugar, descripcion) VALUES (?, ?, ?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE =
      "UPDATE gastos SET fecha = ?, valor_total_sin_iva = ?, iva_total = ?, "
          + "valor_total_con_iva = ?, nombre_usuario = ?, lugar = ?, descripcion = ? "
          + "WHERE id_gasto = ?";

  private static final String SQL_SELECT_BY_ID =
      "SELECT " + COLUMNS + " FROM gastos WHERE id_gasto = ?";

  private static final String SQL_SELECT_ALL =
      "SELECT " + COLUMNS + " FROM gastos ORDER BY fecha DESC, id_gasto DESC";

  private static final String SQL_DELETE = "DELETE FROM gastos WHERE id_gasto = ?";

  private final DataSource dataSource;
  private final GastoPersistenceMapper mapper;

  @Override
  public GastoModel save(final GastoModel gasto) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
      bindWritableColumns(statement, gasto);
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          return gasto.withId(GastoId.of(keys.getInt(1)));
        }
      }
      throw GastoPersistenceException.becauseIdentifierWasNotGenerated();
    } catch (final SQLException exception) {
      log.error("Error saving gasto", exception);
      throw GastoPersistenceException.becauseSaveFailed(exception);
    }
  }

  @Override
  public GastoModel update(final GastoModel gasto) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
      bindWritableColumns(statement, gasto);
      statement.setInt(8, gasto.getId().value());
      if (statement.executeUpdate() == 0) {
        throw GastoNotFoundException.becauseIdDoesNotExist(gasto.getId().value());
      }
      return gasto;
    } catch (final SQLException exception) {
      log.error("Error updating gasto", exception);
      throw GastoPersistenceException.becauseUpdateFailed(gasto.getId().value(), exception);
    }
  }

  @Override
  public Optional<GastoModel> findById(final GastoId id) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {
      statement.setInt(1, id.value());
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(mapper.toDomain(readRow(resultSet)));
        }
        return Optional.empty();
      }
    } catch (final SQLException exception) {
      log.error("Error reading gasto by id", exception);
      throw GastoPersistenceException.becauseFindByIdFailed(id.value(), exception);
    }
  }

  @Override
  public List<GastoModel> findAll() {
    final List<GastoModel> gastos = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
        ResultSet resultSet = statement.executeQuery()) {
      while (resultSet.next()) {
        gastos.add(mapper.toDomain(readRow(resultSet)));
      }
      return gastos;
    } catch (final SQLException exception) {
      log.error("Error listing gastos", exception);
      throw GastoPersistenceException.becauseFindAllFailed(exception);
    }
  }

  @Override
  public boolean deleteById(final GastoId id) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {
      statement.setInt(1, id.value());
      return statement.executeUpdate() > 0;
    } catch (final SQLException exception) {
      log.error("Error deleting gasto", exception);
      throw GastoPersistenceException.becauseDeleteFailed(id.value(), exception);
    }
  }

  private void bindWritableColumns(final PreparedStatement statement, final GastoModel gasto)
      throws SQLException {
    statement.setDate(1, Date.valueOf(gasto.getFecha().value()));
    statement.setBigDecimal(2, gasto.getValorTotalSinIva().value());
    statement.setBigDecimal(3, gasto.getIvaTotal().value());
    statement.setBigDecimal(4, gasto.getValorTotalConIva().value());
    statement.setString(5, gasto.getNombreUsuario().value());
    statement.setString(6, gasto.getLugar().value());
    if (gasto.getDescripcion().isEmpty()) {
      statement.setNull(7, java.sql.Types.LONGVARCHAR);
    } else {
      statement.setString(7, gasto.getDescripcion().value());
    }
  }

  private GastoEntity readRow(final ResultSet resultSet) throws SQLException {
    return new GastoEntity(
        resultSet.getInt("id_gasto"),
        resultSet.getDate("fecha"),
        resultSet.getDouble("valor_total_sin_iva"),
        resultSet.getDouble("iva_total"),
        resultSet.getDouble("valor_total_con_iva"),
        resultSet.getString("nombre_usuario"),
        resultSet.getString("lugar"),
        resultSet.getString("descripcion"));
  }
}
