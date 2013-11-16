update `${tableName}`
  set ${each(columns as column)}`${column}` = ?,
      ${eachlast}`${column}` = ?${/each}
  where `${key}` = ?;
