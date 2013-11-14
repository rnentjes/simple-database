update ${tableName}
  set ${each(columns as column)}${column.key} = ${column.value},
      ${eachlast}${column.key} = ${column.value}${/each}
  where ${each(keys as key)}${key.key} = ${key.value} AND
        ${eachlast}${key.key} = ${key.value}${/each};
