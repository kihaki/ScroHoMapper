package de.koandesign.scrohomapper.widget;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import de.koandesign.scrohomapper.MappingActivity;
import de.koandesign.scrohomapper.NodeBounds;
import de.koandesign.scrohomapper.PathNode;
import de.koandesign.scrohomapper.PointFQuadTree;

/**
 * Created by Kolossus on 30.11.15.
 */
public class MapDrawView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final float PATH_NODE_CIRCLE_RADIUS = 12;
    private static final int POINT_CLOSENESS_THRESHOLD = 100;
    private static final float NO_MOVE_THRESHOLD = 25;
    private static final long LONG_CLICK_DURATION = 500;
    private static final int COMBINE_DISTANCE_THRESHOLD = 80;

    private MapDrawingViewSystem mContainer;

    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint();
    private Paint mSelectedPaint = new Paint();
    private Paint mDebugPathPaint = new Paint();
    private Paint mPathTextPaint = new Paint();

    private ArrayList<PointF> mLines = new ArrayList<>();

    private int mOffsetX, mOffsetY;
    private GestureDetectorCompat mDetector;

    private boolean mSnapToGrid = true;
    private Rect mBounds = new Rect();


    private BitmapRegionDecoder mMapDecoder;
    private Rect mMapDecodeRect = new Rect();
    private BitmapFactory.Options mDecodeOptions;
    private int mDownsamplingFactor = 1;
    private Matrix mBitmapMatrix;
    private int mZoomFactor = 2;
    private PointFQuadTree mPointsTree;
    private PathNode mStartNode;
    private HashMap<PathNode, Boolean> mDrawnNodes = new HashMap<>();
    private PathNode mSelectedNode;

    public MapDrawView(Context context) {
        super(context);
        init();
    }

    public MapDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapDrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MapDrawView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setContainer(MapDrawingViewSystem container) {
        mContainer = container;
    }

    private void init() {
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(36);
        mPaint.getTextBounds("E", 0, 1, mBounds);
        mPaint.setShadowLayer(4.0f, 2.0f, 2.0f, Color.BLACK);

        mPathPaint.setColor(Color.RED);
        mPathPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPathPaint.setStrokeWidth(12);
        mPathPaint.setAntiAlias(true);

        mSelectedPaint.setColor(Color.GREEN);
        mSelectedPaint.setStyle(Paint.Style.STROKE);
        mSelectedPaint.setStrokeWidth(3);
        mSelectedPaint.setAntiAlias(true);

        mDebugPathPaint.setColor(Color.RED);
        mDebugPathPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mDebugPathPaint.setStrokeWidth(2);

        mPathTextPaint.setColor(Color.RED);
        mPathTextPaint.setStyle(Paint.Style.FILL);
        mPathTextPaint.setTextSize(36);
        mPathTextPaint.setTextAlign(Paint.Align.CENTER);
        mPathTextPaint.setAntiAlias(true);

        //mDetector = new GestureDetectorCompat(getContext(), this);
        //mDetector.setOnDoubleTapListener(this);

        mDecodeOptions = new BitmapFactory.Options();
        mDecodeOptions.inSampleSize = mDownsamplingFactor;

        mBitmapMatrix = new Matrix();
        mBitmapMatrix.setScale(mDownsamplingFactor * mZoomFactor, mDownsamplingFactor * mZoomFactor);
    }

    public void setDownsamplingFactor(int factor){
        mDownsamplingFactor = factor;
        mBitmapMatrix.setScale(mDownsamplingFactor * mZoomFactor, mDownsamplingFactor * mZoomFactor);
        mDecodeOptions.inSampleSize = mDownsamplingFactor;
        invalidate();
    }

    public void clearMap() {
        mStartNode = null;
        mPointsTree.clear();
        invalidate();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mMapDecodeRect.set(0, 0, getMeasuredWidth() / mZoomFactor, getMeasuredHeight() / mZoomFactor);
    }

    private void drawMultilineText(String str, int x, int y, Paint paint, Canvas canvas) {
        int      lineHeight = 0;
        int      yoffset    = 0;
        String[] lines      = str.split("\n");

        // set height of each line (height of text + 20%)
        paint.getTextBounds("Ig", 0, 2, mBounds);
        lineHeight = (int) ((float) mBounds.height() * 1.2);
        // draw each line
        for (int i = 0; i < lines.length; ++i) {
            canvas.drawText(lines[i], x, y + yoffset, paint);
            yoffset = yoffset + lineHeight;
        }
    }

    @Override
    public void draw(Canvas c){
        super.draw(c);
        // Draw map
        if(mMapDecoder != null){
            Bitmap mapRegion = mMapDecoder.decodeRegion(mMapDecodeRect, mDecodeOptions);
            c.drawBitmap(mapRegion, mBitmapMatrix, mPaint);

            // Print debug info on screen too
            String consoleText = String.format("w: %dpx, h: %dpx, offsetX %d, offsetY %d, dsf %d", getWidth(), getHeight(), mOffsetX, mOffsetY, mDownsamplingFactor);
            for (final PointF point : mLines) {
                consoleText += "\n" + String.format("Point at x %.2f, y %.2f", point.x, point.y);
            }
            drawMultilineText(consoleText, 100, 100, mPaint, c);
        }

        drawPath(c, mPathPaint);

        drawSelectedNode(c, mSelectedPaint);

        drawRecursiveBounds(mPointsTree, c, mDebugPathPaint);

        // Draw path
        /*for (int i = 0; i < mLines.size(); i++) {
            float scaledOffsetX = mOffsetX * mZoomFactor;
            float scaledOffsetY = mOffsetY * mZoomFactor;
            // Draw way point
            PointF current = mLines.get(i);
            if(i == 0 && mLines.size() > 1){
                // Draw text to indicate start of path
                c.drawText("start", current.x - scaledOffsetX, current.y - scaledOffsetY + 48, mPathTextPaint);
            } else if(i == mLines.size() - 1 && mLines.size() > 1){
                // Draw text to indicate end of path
                c.drawText("end", current.x - scaledOffsetX, current.y - scaledOffsetY + 48, mPathTextPaint);
            }
            c.drawCircle(current.x - scaledOffsetX, current.y - scaledOffsetY, 24, mPathPaint);
            // Draw path
            if(i + 1 < mLines.size()){
                PointF next = mLines.get(i + 1);
                c.drawLine(
                        current.x - scaledOffsetX, current.y - scaledOffsetY,
                        next.x - scaledOffsetX, next.y - scaledOffsetY, mPathPaint);
            }
        }*/
    }

    private void drawSelectedNode(Canvas canvas, Paint paint) {
        if(mSelectedNode != null){
            canvas.drawCircle(mSelectedNode.location.x - getScaledOffsetX(),
                    mSelectedNode.location.y - getScaledOffsetY(),
                    PATH_NODE_CIRCLE_RADIUS * 1.1f, paint);
        }
    }

    private void drawRecursiveBounds(PointFQuadTree tree, Canvas canvas, Paint paint) {
        drawBounds(tree.getBounds(), -getScaledOffsetX(), -getScaledOffsetY(), canvas, paint);
        if(tree.getChildren() != null) {
            for (final PointFQuadTree childTree : tree.getChildren()) {
                drawRecursiveBounds(childTree, canvas, paint);
            }
        }
    }

    private void drawBounds(NodeBounds bounds, float offsetX, float offsetY, Canvas canvas, Paint paint){
        canvas.drawLine(bounds.getMinX() + offsetX, bounds.getMinY() + offsetY, bounds.getMaxX() + offsetX, bounds.getMinY() + offsetY, paint);
        canvas.drawLine(bounds.getMaxX() + offsetX, bounds.getMinY() + offsetY, bounds.getMaxX() + offsetX, bounds.getMaxY() + offsetY, paint);
        canvas.drawLine(bounds.getMaxX() + offsetX, bounds.getMaxY() + offsetY, bounds.getMinX() + offsetX, bounds.getMaxY() + offsetY, paint);
        canvas.drawLine(bounds.getMinX() + offsetX, bounds.getMaxY() + offsetY, bounds.getMinX() + offsetX, bounds.getMinY() + offsetY, paint);
    }

    private void drawPath(Canvas canvas, Paint paint) {
        mDrawnNodes.clear();
        if(mStartNode != null){
            drawPathRecursive(mStartNode, -getScaledOffsetX(), -getScaledOffsetY(), canvas, paint);
        }
    }

    private void drawPathRecursive(PathNode node, float offsetX, float offsetY, Canvas canvas, Paint paint) {
        mDrawnNodes.put(node, true);
        canvas.drawCircle(node.location.x + offsetX, node.location.y + offsetY, PATH_NODE_CIRCLE_RADIUS, paint);
        for (final PathNode child : node.childNodes) {
            canvas.drawLine(node.location.x + offsetX, node.location.y + offsetY, child.location.x + offsetX, child.location.y + offsetY, paint);
            Log.v("DrawNodes", String.format("Child node visited = %b", nodeVisited(node)));
            if(!nodeVisited(child)) {
                drawPathRecursive(child, offsetX, offsetY, canvas, paint);
            }
        }
    }

    private boolean nodeVisited(PathNode node) {
        return mDrawnNodes.containsKey(node);
    }

    private float getScaledOffsetX(){
        return mOffsetX * mZoomFactor;
    }

    private float getScaledOffsetY(){
        return mOffsetY * mZoomFactor;
    }

    float touchX, touchY;
    int startOffsetX, startOffsetY;
    int activePointerId;
    int pointerIndex;
    PointF lastPoint;

    long downTimeStamp;
    float startX, startY;
    PathNode closestNode = null;
    PathNode newNode = null;

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        //mDetector.onTouchEvent(ev);
        int action = ev.getActionMasked();
        int pointerCount = ev.getPointerCount();

        if(pointerCount == 1){
            float x = ev.getX() + getScaledOffsetX();
            float y = ev.getY() + getScaledOffsetY();
            float distX = Math.abs(startX - x);
            float distY = Math.abs(startY - y);
            switch(action){
                case MotionEvent.ACTION_DOWN:
                    downTimeStamp = System.currentTimeMillis();
                    startX = x;
                    startY = y;
                    // Find closest point
                    closestNode = getClosestNode(x, y, POINT_CLOSENESS_THRESHOLD);
                    if(closestNode != null){
                        if(mSelectedNode == closestNode){
                            setSelectedNode(null);
                        } else {
                            setSelectedNode(closestNode);
                        }
                    } else {
                        Log.v("NodeSelect", "No close node found");
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if(closestNode != null) {
                        if (System.currentTimeMillis() - downTimeStamp > LONG_CLICK_DURATION
                                && distX <= NO_MOVE_THRESHOLD && distY <= NO_MOVE_THRESHOLD) {
                        } else {
                            setSelectedNode(null);
                            if(newNode == null && closestNode.canTakeMoreConnections()){
                                // add child node
                                Log.v("NodeSelect", String.format("Closest node is at %f, %f", closestNode.location.x, closestNode.location.y));
                                newNode = new PathNode(new PointF(x, y));
                                closestNode.addChild(newNode);
                                newNode.parent = closestNode;
                            }
                        }

                        if (newNode != null) {
                            // Move child node
                            if (isSnapToGrid()) {
                                float xDist = Math.abs(x - (closestNode.location.x));
                                float yDist = Math.abs(y - (closestNode.location.y));
                                if (xDist > yDist) {
                                    newNode.location.x = x;
                                    newNode.location.y = closestNode.location.y;
                                } else {
                                    newNode.location.x = closestNode.location.x;
                                    newNode.location.y = y;
                                }
                            } else {
                                newNode.location.x = x;
                                newNode.location.y = y;
                            }
                            invalidate();
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    // Check if long press to remove node
                    boolean remove = false;
                    /*if(System.currentTimeMillis() - downTimeStamp > LONG_CLICK_DURATION
                            && distX <= NO_MOVE_THRESHOLD && distY < NO_MOVE_THRESHOLD){
                        // Remove closest node
                        if(closestNode != null) {
                            remove = true;
                        }
                    }*/ //TODO: This will now be done via point menu!
                    if(remove){
                        removeNode(newNode);
                        removeNode(closestNode);
                        invalidate();
                    } else {
                        if (closestNode == null) {
                            if (mStartNode == null) {
                                // Add start node
                                mStartNode = new PathNode(
                                        new PointF(x, y));
                                mPointsTree.add(mStartNode);
                                invalidate();
                            }
                        } else {
                            if(newNode != null) {
                                // Check if newNode is close to another node and connect them!
                                PathNode combineNode = getClosestNode(newNode.location.x, newNode.location.y, COMBINE_DISTANCE_THRESHOLD);
                                if (combineNode != null) {
                                    newNode.parent.addChild(combineNode);
                                    newNode.parent.removeChild(newNode);
                                    //removeNode(newNode);
                                } else {
                                    mPointsTree.add(newNode);
                                }
                            }
                            invalidate();
                        }
                    }
                    newNode = null;
                    return true;
            }
        } else if(pointerCount == 2){
            switch(action){
                case MotionEvent.ACTION_POINTER_DOWN:
                    activePointerId = ev.getPointerId(1);
                    pointerIndex = ev.findPointerIndex(activePointerId);
                    touchX = ev.getX(pointerIndex);
                    touchY = ev.getY(pointerIndex);
                    startOffsetX = mOffsetX;
                    startOffsetY = mOffsetY;
                    Log.v("Moving", String.format("START x %f.3, y %f.3", touchX, touchY));
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int distX = (int) (touchX - ev.getX(pointerIndex));
                    int distY = (int) (touchY - ev.getY(pointerIndex));
                    Log.v("Moving", String.format("distX %d, distY %d", distX, distY));

                    // TODO: Add bounds maybe
                    mOffsetX = startOffsetX + distX;
                    mOffsetY = startOffsetY + distY;

                    int left = mOffsetX;
                    int top = mOffsetY;
                    int right = left + getWidth() / mZoomFactor;
                    int bottom = top + getHeight() / mZoomFactor;

                    mMapDecodeRect.set(left, top, right, bottom);

                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    return true;
            }
        }
        return true;
    }

    private void setSelectedNode(PathNode selected) {
        mSelectedNode = selected;
        if(mSelectedNode != null && !isPointMenuVisible()){
            showPointMenu(mSelectedNode);
        } else if(isPointMenuVisible()) {
            if(mSelectedNode != null){
                updatePointMenu(mSelectedNode);
            } else {
                hidePointMenu();
            }
        }
    }

    private void updatePointMenu(PathNode selectedNode) {
        mContainer.updatePointMenu(selectedNode);
    }

    private void showPointMenu(PathNode selectedNode) {
        mContainer.showPointMenu(selectedNode);
    }

    private void hidePointMenu() {
        mContainer.hidePointMenu();
    }

    private boolean isPointMenuVisible() {
        return mContainer.isPointMenuVisible();
    }

    private PathNode getClosestNode(float x, float y, int searchRadius){
        Collection<PathNode> closeCloud = mPointsTree.searchSquareWithSize(x, y, searchRadius);
        Log.v("NodeSelect", String.format("Found %d nodes close to %f, %f", closeCloud.size(), x, y));
        PathNode closest = getClosestNode(x, y, closeCloud);
        return closest;
    }

    private PathNode getClosestNode(float x, float y, Collection<PathNode> closeCloud) {
        PathNode closest = null;
        for (final PathNode node : closeCloud) {
            if(closest == null){
                closest = node;
            } else {
                if(isCloser(x, y, node, closest)){
                    closest = node;
                }
            }
        }
        return closest;
    }

    private boolean inLeftBounds(int i) {
        return i >= 0;
    }

    private boolean inTopBounds(int i) {
        return i >= 0;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent ev) {
        if(mSnapToGrid && mLines.size() > 0){
            //Only allow straight lines
            PointF lastPoint = mLines.get(mLines.size() - 1);
            float distX = Math.abs((ev.getX() + mOffsetX * mZoomFactor) - lastPoint.x);
            float distY = Math.abs((ev.getY() + mOffsetY * mZoomFactor) - lastPoint.y);
            if(distX < distY){
                mLines.add(new PointF(lastPoint.x, ev.getY() + mOffsetY * mZoomFactor));
            } else {
                mLines.add(new PointF(ev.getX() + mOffsetX * mZoomFactor, lastPoint.y));
            }
        } else {
            addLinePoint(ev.getX(), ev.getY());
        }
        invalidate();
        return true;
    }

    private void addLinePoint(float x, float y) {
        if(mStartNode == null){
            mStartNode = new PathNode(new PointF(x, y));
        } else {
            mStartNode.addChild(new PathNode(new PointF(x, y)));
        }
        //mLines.add(new PointF(x + mOffsetX * mZoomFactor, y + mOffsetY * mZoomFactor));
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    private void removeNode(PathNode node){
        PathNode parent = node.parent;
        if(parent == null){
            // Selected the startNode, can only remove it if it only has one child which can become the new parent
            if(node.childNodes.size() > 1){
                Toast.makeText(getContext(), "Can only remove the start node if it has no more than one child", Toast.LENGTH_LONG).show();
            } if(node.childNodes.size() == 1){
                mStartNode = node.childNodes.get(0);
                mStartNode.parent = null;
            } else {
                // StartNode has no children, do nothing
            }
        } else {
            parent.removeChild(node);
            // add children to new parent
            for (final PathNode childNode : node.childNodes) {
                parent.addChild(childNode);
            }
            node.clearChildren();
        }
        mPointsTree.remove(node);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // Find point that is very close and remove it
        float x = e.getX() + getScaledOffsetX();
        float y = e.getY() + getScaledOffsetY();
        PathNode closest = getClosestNode(x, y, POINT_CLOSENESS_THRESHOLD);
        /*if(mLines.size() > 0){
            ArrayList<PointF> candidates = new ArrayList<>();
            for (int i = 0; i < mLines.size(); i++) {
                PointF candidate = mLines.get(i);
                if(isNearEnough(e.getX() + mOffsetX * mZoomFactor, e.getY() + mOffsetY * mZoomFactor, candidate)){
                    candidates.add(candidate);
                }
            }
            if(candidates.size() > 0){
                if(candidates.size() == 1){
                    mLines.remove(candidates.get(0));
                } else {
                    mLines.remove(findClosest(e.getX() + mOffsetX * mZoomFactor, e.getY() + mOffsetY * mZoomFactor, candidates));
                }
                invalidate();
            }
        }*/
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    /*private PointF findClosest(float x, float y, ArrayList<PointF> candidates) {
        PointF selected = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            if(isCloser(x, y, candidates.get(i), selected)) selected = candidates.get(i);
        }
        return selected;
    }*/

    private boolean isCloser(float x, float y, PathNode current, PathNode toTest) {
        float distCurrent = getDistance(x, y, current);
        float distToTest = getDistance(x, y, toTest);
        return distCurrent > distToTest;
    }

    private float getDistance(float x, float y, PathNode current) {
        return (float) Math.sqrt(
                Math.pow(current.location.x - x, 2) + Math.pow(current.location.y - y, 2)
        );
    }

    static int CLOSENESS_THRESHOLD = 50;
    private boolean isNearEnough(float x, float y, PointF candidate) {
        boolean isCloseHorizontal = Math.abs(candidate.x - x) < CLOSENESS_THRESHOLD;
        boolean isCloseVertical = Math.abs(candidate.y - y) < CLOSENESS_THRESHOLD;
        return isCloseHorizontal && isCloseVertical;
    }

    public void toggleSnapToGrid() {
        mSnapToGrid = !mSnapToGrid;
    }

    public boolean isSnapToGrid() {
        return mSnapToGrid;
    }

    public void setMapAsset(String fileName) {
        AssetManager assetManager = getContext().getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(fileName);
            mMapDecoder = BitmapRegionDecoder.newInstance(istr, false);
            setWillNotDraw(false);
            mPointsTree = new PointFQuadTree(
                    0,
                    mMapDecoder.getWidth() * mZoomFactor / mDownsamplingFactor,
                    0,
                    mMapDecoder.getHeight() * mZoomFactor / mDownsamplingFactor);
            invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeConnectionFromSelectedView(PathNode childNode) {
        if(mSelectedNode != null){
            if(mSelectedNode.childNodes.contains(childNode)) {
                mSelectedNode.removeChild(childNode);
                invalidate();
            } else {
                Toast.makeText(getContext(), "Node to delete is not child of selected node", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
