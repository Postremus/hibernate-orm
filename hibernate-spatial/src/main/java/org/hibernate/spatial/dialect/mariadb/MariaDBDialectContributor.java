/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package org.hibernate.spatial.dialect.mariadb;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.spatial.HSMessageLogger;
import org.hibernate.spatial.KeyedSqmFunctionDescriptors;
import org.hibernate.spatial.contributor.ContributorImplementor;
import org.hibernate.spatial.dialect.mysql.MySQLGeometryJdbcType;

public class MariaDBDialectContributor implements ContributorImplementor {

	private final ServiceRegistry serviceRegistry;

	public MariaDBDialectContributor(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void contributeJdbcTypes(TypeContributions typeContributions) {
		HSMessageLogger.LOGGER.typeContributions( this.getClass().getCanonicalName() );
		typeContributions.contributeJdbcType( MySQLGeometryJdbcType.INSTANCE );
	}

	@Override
	public void contributeFunctions(FunctionContributions functionContributions) {
		HSMessageLogger.LOGGER.functionContributions( this.getClass().getCanonicalName() );
		final KeyedSqmFunctionDescriptors mariaDbFunctions = new MariaDBSqmFunctionDescriptors( functionContributions );
		final SqmFunctionRegistry functionRegistry = functionContributions.getFunctionRegistry();
		mariaDbFunctions.asMap().forEach( (key, desc) -> {
			functionRegistry.register( key.getName(), desc );
			key.getAltName().ifPresent( altName -> functionRegistry.registerAlternateKey( altName, key.getName() ) );
		} );
	}


	@Override
	public ServiceRegistry getServiceRegistry() {
		return this.serviceRegistry;
	}
}
