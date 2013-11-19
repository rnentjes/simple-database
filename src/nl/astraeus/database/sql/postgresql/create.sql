create table ${tableName} (${key} SERIAL,${each(columns as column)}
    ${column.name} ${column.type},${/each}
    primary key(${key})
);
