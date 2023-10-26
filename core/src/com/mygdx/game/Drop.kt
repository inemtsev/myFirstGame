package com.mygdx.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class Drop : ApplicationAdapter() {
    private lateinit var dropImage: Texture
    private lateinit var bucketImage: Texture
    private lateinit var dropSound: Sound
    private lateinit var rainMusic: Music
    private lateinit var levelText: BitmapFont
    private lateinit var scoreText: BitmapFont

    private lateinit var camera: OrthographicCamera
    private lateinit var batch: SpriteBatch

    private val levelTimer: Timer = Timer()
    private val bucket: Rectangle = Rectangle()
    private val raindrops: Array<Rectangle> = Array()
    private var lastDropTime: Long = 0

    private var level = 0
    private var score = 0

    private val touchPos: Vector3 = Vector3()

    override fun create() {
        dropImage = Texture("drop.png")
        bucketImage = Texture("bucket.png")

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"))

        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"))
        rainMusic.isLooping = true
        rainMusic.play()

        levelText = BitmapFont()
        scoreText = BitmapFont()

        camera = OrthographicCamera()
        camera.setToOrtho(false, 800F, 400F)

        batch = SpriteBatch()

        bucket.x = 800F/2 - 64F/2
        bucket.y = 20F
        bucket.width = 64F
        bucket.height = 64F

        levelTimer.scheduleAtFixedRate(0, 10000) { level++ }

        spawnRaindrop()
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0.2f, 1f)

        camera.update()

        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(bucketImage, bucket.x, bucket.y)
        for (raindrop in raindrops) {
            batch.draw(dropImage, raindrop.x, raindrop.y)
        }
        levelText.draw(batch, "Level: $level", 720F, 380F)
        levelText.draw(batch, "Score: $score", 720F, 360F)
        batch.end()

        if(Gdx.input.isTouched) {
            touchPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0F)
            camera.unproject(touchPos)
            bucket.x = touchPos.x - 64F/2
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.deltaTime
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.deltaTime

        if(bucket.x < 0) bucket.x = 0F
        if(bucket.x > 800 - 64) bucket.x = 800 - 64F

        if(TimeUtils.nanoTime() - lastDropTime > 1000000000/level) spawnRaindrop()

        for (raindrop in raindrops) {
            raindrop.y -= 200 * level * Gdx.graphics.deltaTime
            if(raindrop.y + 64 < 0) raindrops.removeValue(raindrop, true)
            if(raindrop.overlaps(bucket)) {
                dropSound.play()
                score += 10
                raindrops.removeValue(raindrop, true)
            }
        }
    }

    override fun dispose() {
        levelTimer.cancel()
        dropImage.dispose()
        bucketImage.dispose()
        dropSound.dispose()
        rainMusic.dispose()
        scoreText.dispose()
        levelText.dispose()
        batch.dispose()
    }

    private fun spawnRaindrop() {
        val raindrop = Rectangle()
        raindrop.x = Math.random().toFloat() * 800
        raindrop.y = 480F
        raindrop.width = 64F
        raindrop.height = 64F
        raindrops.add(raindrop)
        lastDropTime = TimeUtils.nanoTime()
    }
}
