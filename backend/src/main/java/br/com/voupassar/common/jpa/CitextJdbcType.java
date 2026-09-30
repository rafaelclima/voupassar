package br.com.voupassar.common.jpa;

import java.io.Serial;
import java.sql.Types;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcLiteralFormatter;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

/**
 * Mapeamento da extensão PostgreSQL {@code citext} (TASK 3.5 — {@code
 * users.email}).
 *
 * <p>Por que existe: o driver JDBC informa {@code citext} como {@code
 * Types#OTHER}, então {@code String} puro falha no {@code ddl-auto: validate}
 * ("found [citext], expecting [varchar]"); já {@code @JdbcTypeCode(OTHER)}
 * binda o parâmetro como {@code bytea} e quebra {@code citext = ?} no
 * Postgres. Este tipo declara código {@code OTHER} (validação passa) e
 * delega bind/extract ao {@code varchar} (consultas funcionam, inclusive a
 * comparação case-insensitive da extensão).
 */
public class CitextJdbcType implements JdbcType {

  @Serial private static final long serialVersionUID = 1L;

  public static final CitextJdbcType INSTANCE = new CitextJdbcType();

  @Override
  public int getJdbcTypeCode() {
    return Types.OTHER;
  }

  @Override
  public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
    return VarcharJdbcType.INSTANCE.getBinder(javaType);
  }

  @Override
  public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {
    return VarcharJdbcType.INSTANCE.getExtractor(javaType);
  }

  @Override
  public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaType<T> javaType) {
    return VarcharJdbcType.INSTANCE.getJdbcLiteralFormatter(javaType);
  }

  @Override
  public String getFriendlyName() {
    return "CITEXT";
  }

  @Override
  public String toString() {
    return "CitextJdbcType";
  }
}
