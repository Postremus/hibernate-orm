/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.mapping.converted.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;
import org.hibernate.query.BindableType;
import org.hibernate.query.Query;
import org.hibernate.type.Type;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.EntityManagerFactoryScope;
import org.hibernate.testing.orm.junit.Jpa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Vlad Mihalcea
 */
@Jpa(
		annotatedClasses = {
				ConverterTest.Photo.class,
				Employee.class
		}
)
@TestForIssue(jiraKey = "HHH-12662")
public class ConverterTest {

	@BeforeEach
	public void setUp(EntityManagerFactoryScope scope) {
		scope.inTransaction( entityManager -> {
			Photo photo = new Photo();
			photo.setId( 1 );
			photo.setName( "Dorobantul" );
			photo.setCaption( new Caption( "Nicolae Grigorescu" ) );

			entityManager.persist( photo );
		} );
	}

	@AfterEach
	public void tearDown(EntityManagerFactoryScope scope) {
		scope.inTransaction(
				entityManager -> {
					entityManager.createQuery( "delete from Photo" ).executeUpdate();
					entityManager.createQuery( "delete from Employee" ).executeUpdate();
				}
		);
	}

	@Test
	public void testConverterCorrectlyApplied(EntityManagerFactoryScope scope) {
		scope.inTransaction(
				entityManager -> {
					Employee expected = new Employee( 1, "1.2" );
					entityManager.persist( expected );
				}
		);

		scope.inTransaction(
				entityManager -> {
					entityManager
							.createQuery(
									"update Employee e set e.age='1.8.0' WHERE e.id = 1" )
							.executeUpdate();
					entityManager.flush();
				}
		);

		scope.inTransaction(
				entityManager -> {
					Employee emp = entityManager.find( Employee.class, 1 );
					assertNotNull( emp );
					assertEquals( "180", emp.getAge(), "The converter was not properly applied" );
				}
		);

		scope.inTransaction(
				entityManager -> {
					entityManager
							.createQuery(
									"update Employee e set e.age=:age  WHERE e.id = 1" )
							.setParameter( "age", "1.8.1" )
							.executeUpdate();
					entityManager.flush();
				}
		);

		scope.inTransaction(
				entityManager -> {
					Employee emp = entityManager.find( Employee.class, 1 );
					assertNotNull( emp );
					assertEquals( "181", emp.getAge(), "The converter was not properly applied" );
				}
		);

		scope.inTransaction(
				entityManager -> {
					CriteriaBuilder cbuilder = entityManager.getCriteriaBuilder();
					CriteriaUpdate<Employee> cd = cbuilder
							.createCriteriaUpdate( Employee.class );
					Root<Employee> employee = cd.from( Employee.class );
					cd.set( "age", "1.1.3" );
					cd.where( cbuilder.equal( employee.get( "id" ), 1 ) );
					entityManager.createQuery( cd ).executeUpdate();
				}
		);

		scope.inTransaction(
				entityManager -> {
					Employee emp = entityManager.find( Employee.class, 1 );
					assertNotNull( emp );
					assertEquals( "113", emp.getAge(), "The converter was not properly applied" );
				}
		);
	}

	@Test
	public void testJPQLUpperDbValueBindParameter(EntityManagerFactoryScope scope) {
		scope.inTransaction( entityManager -> {
			//tag::basic-attribute-converter-query-parameter-converter-dbdata-example[]
			Photo photo = entityManager.createQuery(
							"select p " +
									"from Photo p " +
									"where upper(caption) = upper(:caption) ", Photo.class )
					.setParameter( "caption", "Nicolae Grigorescu" )
					.getSingleResult();
			//end::basic-attribute-converter-query-parameter-converter-dbdata-example[]

			assertEquals( "Dorobantul", photo.getName() );
		} );
	}

	@Test
	public void testJPQLUpperAttributeValueBindParameterType(EntityManagerFactoryScope scope) {
		scope.inTransaction( entityManager -> {
			//tag::basic-attribute-converter-query-parameter-converter-object-example[]
			SessionFactoryImplementor sessionFactory = entityManager.getEntityManagerFactory()
					.unwrap( SessionFactoryImplementor.class );
			final MappingMetamodelImplementor mappingMetamodel = sessionFactory
					.getRuntimeMetamodels()
					.getMappingMetamodel();

			Type captionType = mappingMetamodel
					.getEntityDescriptor( Photo.class )
					.getPropertyType( "caption" );

			Photo photo = (Photo) entityManager.createQuery(
							"select p " +
									"from Photo p " +
									"where upper(caption) = upper(:caption) ", Photo.class )
					.unwrap( Query.class )
					.setParameter(
							"caption",
							new Caption( "Nicolae Grigorescu" ),
							(BindableType) captionType
					)
					.getSingleResult();
			//end::basic-attribute-converter-query-parameter-converter-object-example[]

			assertEquals( "Dorobantul", photo.getName() );
		} );
	}

	//tag::basic-attribute-converter-query-parameter-object-example[]
	public static class Caption {

		private String text;

		public Caption(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			Caption caption = (Caption) o;
			return text != null ? text.equals( caption.text ) : caption.text == null;

		}

		@Override
		public int hashCode() {
			return text != null ? text.hashCode() : 0;
		}
	}
	//end::basic-attribute-converter-query-parameter-object-example[]

	//tag::basic-attribute-converter-query-parameter-converter-example[]
	public static class CaptionConverter
			implements AttributeConverter<Caption, String> {

		@Override
		public String convertToDatabaseColumn(Caption attribute) {
			return attribute.getText();
		}

		@Override
		public Caption convertToEntityAttribute(String dbData) {
			return new Caption( dbData );
		}
	}
	//end::basic-attribute-converter-query-parameter-converter-example[]

	//tag::basic-attribute-converter-query-parameter-entity-example[]
	@Entity(name = "Photo")
	public static class Photo {

		@Id
		private Integer id;

		private String name;

		@Convert(converter = CaptionConverter.class)
		private Caption caption;

		//Getters and setters are omitted for brevity
		//end::basic-attribute-converter-query-parameter-entity-example[]

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Caption getCaption() {
			return caption;
		}

		public void setCaption(Caption caption) {
			this.caption = caption;
		}
		//tag::basic-attribute-converter-query-parameter-entity-example[]
	}
	//end::basic-attribute-converter-query-parameter-entity-example[]
}
