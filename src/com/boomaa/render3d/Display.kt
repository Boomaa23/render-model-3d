package com.boomaa.render3d

import com.boomaa.render3d.gfx.MousePanel
import com.boomaa.render3d.gfx.PGN
import com.boomaa.render3d.gfx.Poly
import com.boomaa.render3d.gfx.Shader
import com.boomaa.render3d.math.Matrix
import com.boomaa.render3d.math.Vec
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JFrame
import kotlin.math.*


object Display: JFrame("CAD Renderer 3D") {
    val poly = ArrayList<Poly>()
    val renderPanel: MousePanel = object : MousePanel() {
        override fun paintComponent(g: Graphics) {
            val g2 = g as Graphics2D
            g2.color = Color.BLACK
            g2.fillRect(0, 0, width, height)
            render(g2)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        PGN()
        super.getContentPane().layout = BorderLayout()
        super.getContentPane().add(renderPanel)
        super.setSize(400, 400)
        super.setVisible(true)
    }

    // ref http://blog.rogach.org/2015/08/how-to-create-your-own-simple-3d-render.html
    fun render(g2: Graphics2D) {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val zBuffer = DoubleArray(img.width * img.height)
        Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY)

        //TODO triangulate polygons into triangles for complex shape (non-triangle) rendering
        for (t in poly) {
            val v1 = applyRotation(t, 0)
            val v2 = applyRotation(t, 1)
            val v3 = applyRotation(t, 2)

            val minX = max(0.0, ceil(min(v1.coords[0], min(v2.coords[0], v3.coords[0])))).toInt()
            val maxX = min(img.width - 1.0, floor(max(v1.coords[0], max(v2.coords[0], v3.coords[0])))).toInt()
            val minY = max(0.0, ceil(min(v1.coords[1], min(v2.coords[1], v3.coords[1])))).toInt()
            val maxY = min(img.height - 1.0, floor(max(v1.coords[1], max(v2.coords[1], v3.coords[1])))).toInt()

            val cross = v2.add(v1.negate())!!.crossProduct(v3.add(v1.negate())!!)!!.toUnitVec()
            val triangleArea: Double = (v1.coords[1] - v3.coords[1]) * (v2.coords[0] - v3.coords[0]) + (v2.coords[1] - v3.coords[1]) * (v3.coords[0] - v1.coords[0])

            for (y in minY..maxY) {
                for (x in minX..maxX) {
                    val b1 = ((y - v3.coords[1]) * (v2.coords[0] - v3.coords[0]) + (v2.coords[1] - v3.coords[1]) * (v3.coords[0] - x)) / triangleArea
                    val b2 = ((y - v1.coords[1]) * (v3.coords[0] - v1.coords[0]) + (v3.coords[1] - v1.coords[1]) * (v1.coords[0] - x)) / triangleArea
                    val b3 = ((y - v2.coords[1]) * (v1.coords[0] - v2.coords[0]) + (v1.coords[1] - v2.coords[1]) * (v2.coords[0] - x)) / triangleArea
                    val depth = (b1 * v1.coords[2]) + (b2 * v2.coords[2]) + (b3 * v3.coords[2])
                    val zIndex = (y * img.width) + x
                    if (b1 in 0.0..1.0 && b2 in 0.0..1.0 && b3 in 0.0..1.0) {
                        if (zBuffer[zIndex] < depth) {
                            img.setRGB(x, y, Shader.getShade(t.color, abs(cross.coords[2])).rgb)
                            zBuffer[zIndex] = depth
                        }
                    }
                }
            }
            g2.drawImage(img, 0, 0, null)
        }
    }

    private fun applyRotation(p: Poly, n: Int): Vec {
        return Matrix.Rotation.xz(Math.toRadians(renderPanel.heading))
            .matrixMultiply(Matrix.Rotation.yz(Math.toRadians(renderPanel.pitch)))!!
            .vectorMultiply(p.matrix.getCol(n)!!)!!
            .add(Vec(width / 2.0, height / 2.0, 0.0))!!
    }
}