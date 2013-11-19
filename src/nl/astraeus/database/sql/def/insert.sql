insert into ${tableName}
  (${each(columns as column)}${column}, ${eachlast}${column}${/each}) values
  (${each(columns as column)}?, ${eachlast}?${/each});
