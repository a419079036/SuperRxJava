package mobi.vhly.superrxjava.services;

/**
 * Created by vhly[FR].
 * <p>
 * Author: vhly[FR]
 * Email: vhly@163.com
 * Date: 2016/10/27
 */

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * 豆瓣的 API 定义
 */
public interface DoubanService {

    @GET("movie/top250")
    Observable<String> getMovieTop250(
            @Query("start") int start,
            @Query("count") int count);

}
