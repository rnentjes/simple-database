create ${if(unique)}unique${/if} index IF NOT EXISTS IDX_${tableName}_${column.name} on ${tableName}(${column.name})
