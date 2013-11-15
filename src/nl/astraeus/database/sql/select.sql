select ${each(keys as key)}${key}, ${eachlast}${key}${/each}
  from ${tableName}
  where ${each(keys as key)}${key} = ?,
    ${eachlast}${key} = ?${/each};
