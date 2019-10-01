 ### Hibernate
 Question | Small Description
 ------------ | -------------|
 [Map calculated properties with JPA and Hibernate](https://vladmihalcea.com/how-to-map-calculated-properties-with-jpa-and-hibernate-formula-annotation/) | Mapping calculated entity properties. Ex- if we need one property which will count the number of row of that particular entity/table.We can use this annotation and pass appropiate SQL functions.|
 [ManyToOne Relationship](/src/test/java/hibernate/association/manyToOne) | ManyToOne relationship is generate optimal SQL query for every DML query.Basically it hold the foreign key relationship. |
 [ManyToOne association using a non-Primary Key column](https://vladmihalcea.com/how-to-map-a-manytoone-association-using-a-non-primary-key-column/) | @ManyToOne annotation when the Foreign Key column on the client side references a non-Primary Key column on the parent side.|
 [Unidirectional/Bidirectional @OneToMany](https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/) | Unidirectional @OneToMany is not so good while performing DML opertaion. Better to make it bidirectional. |
 [store date, time, and timestamps in UTC time zone](https://vladmihalcea.com/how-to-store-date-time-and-timestamps-in-utc-time-zone-with-jdbc-and-hibernate/) | Dealing with time zones is always challenging. As a rule of thumb, it’s much easier if all date/time values are stored in the UTC format, and, if necessary, dealing with time zone conversions in the UI only.
This article is going to demonstrate how you can accomplish this task with JDBC and the awesome hibernate.jdbc.time_zone configuration property. |

