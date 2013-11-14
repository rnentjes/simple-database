update ${tableName}
  set ${each(columns as column)}${column.key} = ?,
      ${eachlast}${column.key} = ?${/each}
  where ${each(keys as key)}${key.key} = ? AND
        ${eachlast}${key.key} = ?${/each};
