/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.rm.datasource.undo;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author guoyao
 * @date 2019/4/19
 */
public class UndoLogManagerTest {


    private static final int APPEND_IN_SIZE = 10;

    private static final int LIMIT_SIZE = 10;

    private static final String THE_APPEND_IN_SIZE_PARAM_String = " (?,?,?,?,?,?,?,?,?,?) ";

    @Test
    public void testBatchDeleteUndoLog() throws Exception {
        Set<String> xids = new HashSet<>();
        for (int i = 0;i < APPEND_IN_SIZE;i++){
            xids.add(UUID.randomUUID().toString());
        }
        Set<Long> branchIds = new HashSet<>();
        for (int i = 0;i < APPEND_IN_SIZE;i++){
            branchIds.add(Long.valueOf(i));
        }
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        UndoLogManager.batchDeleteUndoLog(xids, branchIds, LIMIT_SIZE, connection);

        //verify
        for (int i = 1;i <= APPEND_IN_SIZE;i++){
            verify(preparedStatement).setLong(eq(i),anyLong());
        }
        for (int i = APPEND_IN_SIZE + 1;i <= APPEND_IN_SIZE * 2;i++){
            verify(preparedStatement).setString(eq(i),anyString());
        }
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void testToBatchDeleteUndoLogSql() {
        String expectedSqlString="DELETE FROM undo_log WHERE  branch_id IN " +
                THE_APPEND_IN_SIZE_PARAM_String +
                " AND xid IN " +
                THE_APPEND_IN_SIZE_PARAM_String +
                " LIMIT " + LIMIT_SIZE;
        String batchDeleteUndoLogSql = UndoLogManager.toBatchDeleteUndoLogSql(APPEND_IN_SIZE, APPEND_IN_SIZE, LIMIT_SIZE);
        System.out.println(batchDeleteUndoLogSql);
        assertThat(batchDeleteUndoLogSql).isEqualTo(expectedSqlString);
    }

    @Test
    public void testAppendInParam() {
        StringBuilder sqlBuilder = new StringBuilder();
        UndoLogManager.appendInParam(APPEND_IN_SIZE, sqlBuilder);
        assertThat(sqlBuilder.toString()).isEqualTo(THE_APPEND_IN_SIZE_PARAM_String);
    }

}
