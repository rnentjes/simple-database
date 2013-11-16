select ${each(columns as column)}`${column}`,
       ${eachlast}`${column}`${/each}
  from `${tableName}`
  where `${key}` = ?;
