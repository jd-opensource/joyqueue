package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.AppTokenDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * AppTokenRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class AppTokenRepository extends BaseRepository {

    private static final String TABLE = "app_token";
    private static final String COLUMNS = "id, app, token, effective_time, expiration_time";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String GET_BY_APP_AND_CODE = String.format("SELECT %s FROM %s WHERE app = ? AND code = ?", COLUMNS, TABLE);
    private static final String GET_BY_APP = String.format("SELECT %s FROM %s WHERE app = ? AND code = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?)", TABLE, COLUMNS);

    public AppTokenRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public AppTokenDTO getById(long id) {
        return queryOnce(AppTokenDTO.class, GET_BY_ID, id);
    }

    public AppTokenDTO getByAppAndToken(String app, String token) {
        return queryOnce(AppTokenDTO.class, GET_BY_APP_AND_CODE, app, token);
    }

    public List<AppTokenDTO> getByApp(String app) {
        return query(AppTokenDTO.class, GET_BY_APP, app);
    }

    public AppTokenDTO add(AppTokenDTO appTokenDTO) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        insert(ADD, appTokenDTO.getId(), appTokenDTO.getApp(), appTokenDTO.getToken(),
                format.format(appTokenDTO.getEffectiveTime()), format.format(appTokenDTO.getExpirationTime()));
        return appTokenDTO;
    }
}