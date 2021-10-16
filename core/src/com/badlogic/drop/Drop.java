package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class Drop extends ApplicationAdapter {

    private Texture dropImage;
    private Texture bucketImage;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Rectangle bucket;
    private Array<Rectangle> raindrops;
    private long lastDropTime; //最後に雨粒が発生した時間を記録しておくため

    @Override
    public void create () {

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();

        //ドロップレットとバケットの画像をそれぞれ64x64ピクセルでロードします
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 20;
        bucket.width = 64;
        bucket.height = 64;

        //雨粒のインスタンスを作成
        raindrops = new Array<Rectangle>();
        spawnRaindrop();

    }

    //雨粒を生成するメソッド
    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render () {
        //スクリーンの色を指定
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        //batchを開始して、バケツと雨粒を描画する
        batch.begin();
        batch.draw(bucketImage, bucket.x, bucket.y);
        for(Rectangle raindrop: raindrops) {
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        batch.end();

        //キー入力による操作設定
        if(Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }

        if(Gdx.input.isKeyPressed(Keys.LEFT))bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Keys.RIGHT))bucket.x += 200 * Gdx.graphics.getDeltaTime();

        //バケツが画面外にはみ出さないようにする処理
        if(bucket.x < 0) bucket.x = 0;
        if(bucket.x > 800 - 64) bucket.x = 800 - 64;

        //最後に雨粒を新規作成してからどれだけ時間が経過しているかを確認して必要であれば雨粒を新規作成する
        if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

        //雨粒を毎秒200ピクセルの速度を維持して落下させる
        Iterator<Rectangle> iter = raindrops.iterator();
        while(iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();

          //落下中、もしバケツに接触した場合、その時点で雨粒を削除
            if(raindrop.overlaps(bucket)) {
                iter.remove();
            }

            //画面外に出た場合、雨粒を削除
            if(raindrop.y + 64 < 0) iter.remove();
         }

    }

    @Override
    public void dispose () {
        dropImage.dispose();
        bucketImage.dispose();
        batch.dispose();
    }
}
