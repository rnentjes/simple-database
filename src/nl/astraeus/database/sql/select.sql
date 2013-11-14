select ${each(keys as key)}${key.key}, ${eachlast}${key.key}${/each}
  from ${tableName}
  where ${each(keys as key)}${key.key} = ${key.value},
    ${eachlast}${key.key} = ${key.value}${/each};
