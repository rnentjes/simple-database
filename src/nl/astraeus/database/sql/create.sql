create table ${tableName} (${each(columns as column)}
    ${column.name} ${column.type},${/each}
    primary key(${each(keys as key)}${key}, ${eachlast}${key}${/each})
);
