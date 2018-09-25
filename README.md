# ArcProgressView

代码示例：

```
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arc_progress_view_layout);
        mArcProgressView = findViewById(R.id.arc_view);
        List<ArcProgressView.Line> lines = new ArrayList<>();
        //所以的percent加起来超过1就抛弃了
        lines.add(new ArcProgressView.Line(Color.parseColor("#36BB8F"),0.2f));
        ArcProgressView.Line line = new ArcProgressView.Line(Color.parseColor("#BA55D3"),0.8f);
        line.setColors(new int[]{Color.parseColor("#6495ED"),Color.parseColor("#000080")});
        lines.add(line);
        mArcProgressView.setLine(lines);
        mArcProgressView.setHeadText("总价值(RMB)");
        //px
        mArcProgressView.setHeadTextSize(33);
        mArcProgressView.setHeadTextColor(Color.parseColor("#717273"));
        mArcProgressView.setBodyText("42328.69");
        //px
        mArcProgressView.setBodyTextSize(90);
        mArcProgressView.setBodyTextColor(Color.parseColor("#303030"));
        mArcProgressView.animator(3000);

    }
```

截图：

![](https://github.com/yeshuwei/ArcProgressView/blob/master/device-2018-09-25-154520.png)
