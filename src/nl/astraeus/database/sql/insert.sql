insert into ${tableName}
  (${each(columns as column)}${column.key}, ${eachlast}${column.key}${/each}) values
  (${each(columns as column)}${column.value}, ${eachlast}${column.value}${/each});
