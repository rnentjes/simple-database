update ${tableName}
  set ${each(columns as column)}${column} = ?,
      ${eachlast}${column} = ?${/each}
  where ${each(keys as key)}${key} = ? AND
        ${eachlast}${key} = ?${/each};
