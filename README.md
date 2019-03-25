# RadarView
可定制且带有动画效果的雷达图

<img src="https://github.com/frasker/RadarView/blob/master/captures/radar.gif" width="30%">

## 设置方式
RadarView通过`RadarData`来控制维度，默认是五个维度。
```
        List<RadarData> radarDatas = new ArrayList<>();
        radarDatas.add(new RadarData("社区活跃度", 0.8));
        radarDatas.add(new RadarData("社区影响力", 0.6));
        radarDatas.add(new RadarData("社区共享度", 0.4));
        radarDatas.add(new RadarData("社区积极性", 0.9));
        radarDatas.add(new RadarData("社区贡献度", 0.2));
        radarView.setDataList(radarDatas);
```
## 动画支持
RadarView内部封装了动画效果，通过`playAnimation`来实现过度动画。如果需要首次展示时不显示内容，可以配置`app:r_showAnimation=true`实现

## 数据小圆点、文字位置支持、雷达背景重写
由于产品要求的多样性，数据的圆点、文字展示位置以及背景通常有自己的要求，因此RadarView支持对相关方法进行重写，实现自己的逻辑。
```
//数据小圆点
@Override
protected void drawDataCircle(Canvas canvas, float x, float y) {
}
//文字位置支持
@Override
protected void drawText(Canvas canvas, float[] dotX, float[] dotY, List<RadarData> dataList) {
}
//雷达背景支持
@Override
protected void drawWebRegion(Canvas canvas, int axisTickCount, int count, float[][] arrayDotX, float[][] arrayDotY) {
}
```

## 支持属性

```
//雷达图半径，如果不设置默认宽高最小值的0.6
app:r_radius
//雷达背景图分段数
app:r_axisTickCount
//雷达区颜色
app:r_radarBgColor
//雷达区线颜色
app:r_radarLineColor
//数据区颜色
app:r_valueColor
//数据线颜色
app:r_valueLineColor
//首次需要展示动画
app:r_showAnimation
//文本颜色
app:r_textColor
//文本大小
app:r_textSize
```
## 依赖
```
implementation 'com.github.frasker:RadarView:v1.0.1-alpha'
```
