/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.BindableType;
import org.hibernate.query.hql.spi.SqmCreationState;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SqmExpressible;
import org.hibernate.type.descriptor.java.JdbcDateJavaType;
import org.hibernate.type.descriptor.java.JdbcTimeJavaType;
import org.hibernate.type.descriptor.java.JdbcTimestampJavaType;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class SqmExpressionHelper {
	public static <T> SqmExpressible<T> toSqmType(BindableType<T> parameterType, SqmCreationState creationState) {
		return toSqmType( parameterType, creationState.getCreationContext().getJpaMetamodel().getTypeConfiguration() );
	}

	public static <T> SqmExpressible<T> toSqmType(BindableType<T> anticipatedType, NodeBuilder nodeBuilder) {
		return toSqmType( anticipatedType, nodeBuilder.getTypeConfiguration() );
	}

	public static <T> SqmExpressible<T> toSqmType(BindableType<T> anticipatedType, TypeConfiguration typeConfiguration) {
		return toSqmType( anticipatedType, typeConfiguration.getSessionFactory() );
	}

	public static <T> SqmExpressible<T> toSqmType(BindableType<T> anticipatedType, SessionFactoryImplementor sessionFactory) {
		if ( anticipatedType == null ) {
			return null;
		}
		final SqmExpressible<T> sqmExpressible = anticipatedType.resolveExpressible( sessionFactory );
		assert sqmExpressible != null;

		return sqmExpressible;

	}

	public static SqmLiteral<Timestamp> timestampLiteralFrom(String literalText, SqmCreationState creationState) {
		final Timestamp literal = Timestamp.valueOf(
				LocalDateTime.from( JdbcTimestampJavaType.LITERAL_FORMATTER.parse( literalText ) )
		);

		return new SqmLiteral<>(
				literal,
				creationState.getCreationContext().getJpaMetamodel().getTypeConfiguration().standardBasicTypeForJavaType( Timestamp.class ),
				creationState.getCreationContext().getQueryEngine().getCriteriaBuilder()
		);
	}

	public static SqmLiteral<Integer> integerLiteral(String literalText, SqmCreationState creationState) {
		return integerLiteral( literalText, creationState.getCreationContext().getQueryEngine() );
	}

	public static SqmLiteral<Integer> integerLiteral(String literalText, QueryEngine queryEngine) {
		return integerLiteral( Integer.parseInt( literalText ), queryEngine );
	}

	public static SqmLiteral<Integer> integerLiteral(int value, QueryEngine queryEngine) {
		return new SqmLiteral<>(
				value,
				queryEngine.getCriteriaBuilder().getIntegerType(),
				queryEngine.getCriteriaBuilder()
		);
	}

	public static SqmLiteral<Date> dateLiteralFrom(String literalText, SqmCreationState creationState) {
		final LocalDate localDate = LocalDate.from( JdbcDateJavaType.LITERAL_FORMATTER.parse( literalText ) );
		final Date literal = new Date( localDate.toEpochDay() );

		return new SqmLiteral<>(
				literal,
				creationState.getCreationContext().getJpaMetamodel().getTypeConfiguration().standardBasicTypeForJavaType( Date.class ),
				creationState.getCreationContext().getQueryEngine().getCriteriaBuilder()
		);
	}

	public static SqmLiteral<Time> timeLiteralFrom(String literalText, SqmCreationState creationState) {
		final LocalTime localTime = LocalTime.from( JdbcTimeJavaType.LITERAL_FORMATTER.parse( literalText ) );
		final Time literal = Time.valueOf( localTime );

		return new SqmLiteral<>(
				literal,
				creationState.getCreationContext().getJpaMetamodel().getTypeConfiguration().standardBasicTypeForJavaType( Time.class ),
				creationState.getCreationContext().getQueryEngine().getCriteriaBuilder()
		);
	}
}
