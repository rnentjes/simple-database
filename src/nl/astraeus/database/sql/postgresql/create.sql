create table ${tableName} (${key} BIGSERIAL,${each(columns as column)}
    ${column.name} ${column.type},${/each}
    primary key(${key})
)
