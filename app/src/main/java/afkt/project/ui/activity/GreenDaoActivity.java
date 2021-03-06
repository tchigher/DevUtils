package afkt.project.ui.activity;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.greendao.query.DeleteQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import afkt.project.R;
import afkt.project.base.app.BaseToolbarActivity;
import afkt.project.db.GreenManager;
import afkt.project.db.Note;
import afkt.project.db.NotePicture;
import afkt.project.db.NotePictureDao;
import afkt.project.db.NoteType;
import afkt.project.ui.adapter.GreenDaoAdapter;
import afkt.project.ui.widget.BaseRefreshView;
import butterknife.BindView;
import butterknife.OnClick;
import dev.assist.PageAssist;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.toast.ToastTintUtils;
import dev.utils.common.ChineseUtils;
import dev.utils.common.RandomUtils;

/**
 * detail: GreenDao 使用
 * @author Ttt
 * <pre>
 *     官方文档
 *     @see <a href="https://greenrobot.org/greendao/documentation/modelling-entities"/>
 *     SQL 语句写到累了? 试试 GreenDAO
 *     @see <a href="https://www.jianshu.com/p/11bdd9d761e6"/>
 *     Android GreenDao 数据库
 *     @see <a href="https://www.jianshu.com/p/26c60d59e76d"/>
 *     Android ORM 框架 : GreenDao 使用详解 ( 进阶篇 )
 *     @see <a href="https://blog.csdn.net/speedystone/article/details/74193053"/>
 * </pre>
 */
public class GreenDaoActivity extends BaseToolbarActivity {

    // = View =
    @BindView(R.id.vid_agd_refresh)
    BaseRefreshView vid_agd_refresh;

    @Override
    public int getLayoutId() {
        return R.layout.activity_green_dao;
    }

    @Override
    public void initValue() {
        super.initValue();

        ToastTintUtils.info("侧滑可进行删除, 长按拖动位置");

        // 初始化布局管理器、适配器
        vid_agd_refresh.setAdapter(new GreenDaoAdapter())
                .setPageAssist(new PageAssist<>(0, 8));
        // 加载数据
        loadData(true);
    }

    @Override
    public void initListener() {
        super.initListener();
        // 刷新事件
        vid_agd_refresh.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                loadData(true);
            }

            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                loadData(false);
            }
        });

        // =================
        // = Item 滑动处理 =
        // =================

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            /**
             * 获取动作标识
             * 动作标识分 : dragFlags 和 swipeFlags
             * dragFlags : 列表滚动方向的动作标识 ( 如竖直列表就是上和下, 水平列表就是左和右 )
             * wipeFlags : 与列表滚动方向垂直的动作标识 ( 如竖直列表就是左和右, 水平列表就是上和下 )
             */
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // 如果你不想上下拖动, 可以将 dragFlags = 0
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

                // 如果你不想左右滑动, 可以将 swipeFlags = 0
                int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

                // 最终的动作标识 ( flags ) 必须要用 makeMovementFlags() 方法生成
                int flags = makeMovementFlags(dragFlags, swipeFlags);
                return flags;
            }

            /**
             * 是否开启 item 长按拖拽功能
             */
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                GreenDaoAdapter adapter = vid_agd_refresh.getAdapter();
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(adapter.getData(), fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            /**
             * 当 item 侧滑出去时触发 ( 竖直列表是侧滑, 水平列表是竖滑 )
             * @param viewHolder
             * @param direction 滑动的方向
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) {
                    GreenDaoAdapter adapter = vid_agd_refresh.getAdapter();
                    Note note = adapter.getData().remove(position);
                    adapter.notifyItemRemoved(position);
                    // 删除文章
//                    GreenManager.getNoteDao().delete(note);
                    GreenManager.getNoteDao().deleteByKey(note.getId());
                    // 删除文章图片
                    DeleteQuery<NotePicture> deleteQuery = GreenManager.getNotePictureDao().queryBuilder()
                            .where(NotePictureDao.Properties.NoteId.eq(note.getId())).buildDelete();
                    deleteQuery.executeDeleteWithoutDetachingEntities();
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(vid_agd_refresh.getRecyclerView());
    }

    @OnClick({R.id.vid_agd_add_btn})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.vid_agd_add_btn:
                if (vid_agd_refresh.getAdapter().getData().isEmpty()) { // 不存在数据
                    randomData(13);
                    // 加载数据
                    loadData(true);
                } else {
                    randomData(RandomUtils.getRandom(2, 6));
                    // 进行提示
                    ToastTintUtils.success("添加成功, 上拉加载数据");
                }
                break;
        }
    }

    // ============
    // = 数据相关 =
    // ============

    /**
     * 随机添加数据
     */
    private void randomData() {
        Note note = new Note();
        note.setDate(new Date());
        note.setText(ChineseUtils.randomWord(RandomUtils.getRandom(6, 15)));
        note.setComment(ChineseUtils.randomWord(RandomUtils.getRandom(12, 50)));
        note.setType(NoteType.values()[RandomUtils.getRandom(0, 3)]);
        // 添加数据
        Long noteId = GreenManager.getNoteDao().insert(note);
        // 不等于文本
        if (note.getType() != NoteType.TEXT) {
            List<NotePicture> pictures = new ArrayList<>();
            for (int i = 0, len = RandomUtils.getRandom(1, 5); i < len; i++) {
                NotePicture notePicture = new NotePicture();
                notePicture.setNoteId(noteId);
                notePicture.setPicture(String.format("https://picsum.photos/id/%s/30%s", RandomUtils.getRandom(5, 21), RandomUtils.getRandom(0, 10)));
                pictures.add(notePicture);
            }
            GreenManager.getNotePictureDao().insertInTx(pictures);
        }
    }

    /**
     * 随机添加数据
     * @param number 随机数量
     */
    private void randomData(int number) {
        for (int i = 0; i < number; i++) {
            randomData();
        }
    }

    // =

    /**
     * 加载数据
     * @param refresh 是否刷新
     */
    private void loadData(boolean refresh) {
        PageAssist pageAssist = vid_agd_refresh.getPageAssist();
        GreenDaoAdapter adapter = vid_agd_refresh.getAdapter();
        // 刷新则重置页数
        if (refresh) pageAssist.reset();

        List<Note> notes = offsetLimitCalculate(refresh);

//        // 正常只需要这个, 没有添加功能则不需要计算偏差值
//        List<Note> notes = GreenManager.getNoteDao().queryBuilder()
//                .offset(pageAssist.getPageNum() * pageAssist.getPageSize())
//                .limit(pageAssist.getPageSize()).list();

        // 存在数据则累加页数
        if (!notes.isEmpty()) pageAssist.nextPage();

        if (refresh) {
            adapter.setNewInstance(notes);
        } else {
            adapter.addData(notes);
            adapter.notifyDataSetChanged();
        }

        // 结束刷新、加载
        vid_agd_refresh.finishRefreshOrLoad(refresh);
    }

    /**
     * 页数偏移值计算处理
     * <pre>
     *     为什么需要特殊计算 :
     *     正常到最后一页没有数据是禁止加载更多
     *     为了演示 GreenDao 分页实现功能, 显示添加数据按钮并且不限制加载更多功能
     *     可能导致新增数据 + 原有数据刚好 = 页数 * 每页条数, 导致无法加载下一页
     * </pre>
     * @param refresh 是否刷新
     */
    private List<Note> offsetLimitCalculate(boolean refresh) {
        int offset, limit;

        int pageSize = vid_agd_refresh.getPageAssist().getPageSize();

        if (refresh) {
            offset = 0;
            limit = pageSize;
        } else {
            // 获取当前数据条数
            int size = vid_agd_refresh.getAdapter().getData().size();
            // 计算当前数据实际页数
            int page = size / pageSize;
            int remainder = size % pageSize;

            if (remainder == 0) {
                offset = page * pageSize;
                limit = pageSize;
            } else {
                int diff = Math.abs(page * pageSize - size);
                offset = size;
                limit = pageSize * 2 - diff;
            }
        }
        DevLogger.dTag(mTag, "offset: " + offset + ", limit: " + limit);
        // 请求数据
        return GreenManager.getNoteDao().queryBuilder()
                .offset(offset)
                .limit(limit).list();
    }
}