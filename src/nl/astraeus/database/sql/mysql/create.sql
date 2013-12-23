create table ${tableName} (${key} BIGINT NOT NULL AUTO_INCREMENT,${each(columns as column)}
    ${column.name} ${column.type},${/each}
    primary key(${key})
)
