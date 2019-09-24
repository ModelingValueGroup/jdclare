package org.modelingvalue.jdclare.swing.examples.newton;

import static java.lang.Math.*;
import static org.modelingvalue.jdclare.DClare.*;

import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;

public interface BallBallPair extends DStruct2<Ball, Ball>, CollisionPair {

    @Property(key = 0)
    Ball a();

    @Property(key = 1)
    Ball b();

    @Override
    @Property
    default double preCollisionTime() {
        DPoint va = pre(a(), Ball::velocity);
        DPoint vb = pre(b(), Ball::velocity);
        DPoint pa = pre(a(), Ball::position);
        DPoint pb = pre(b(), Ball::position);
        return collisionTime(va, vb, pa, pb);
    }

    @Override
    default double postCollisionTime() {
        DPoint va = a().solVelocity();
        DPoint vb = b().solVelocity();
        DPoint pa = a().solPosition();
        DPoint pb = b().solPosition();
        return collisionTime(va, vb, pa, pb);
    }

    private double collisionTime(DPoint va, DPoint vb, DPoint pa, DPoint pb) {
        if (!va.equals(DPoint.NULL) || !vb.equals(DPoint.NULL)) {
            Table table = a().table();
            DPoint dv = va.minus(vb);
            DPoint dp = pa.minus(pb);
            double a = dv.dot(dv);
            double b = 2 * dp.dot(dv);
            double c = dp.dot(dp);
            double d = pow(b, 2.0) - 4 * a * (c - 4 * table.ballRadiusPow());
            if (d >= 0) {
                double sqrt = sqrt(d);
                double t1 = (-b + sqrt) / (2 * a);
                double t2 = (-b - sqrt) / (2 * a);
                return Math.min(t1, t2);
            }
        }
        return Double.MAX_VALUE;
    }

    @Override
    default DPoint velocity(Ball ball) {
        return ball.equals(a()) ? aVelocity() : ball.equals(b()) ? bVelocity() : ball.solVelocity();
    }

    @Default
    @Property
    default DPoint aVelocity() {
        return DPoint.NULL;
    }

    @Default
    @Property
    default DPoint bVelocity() {
        return DPoint.NULL;
    }

    @Rule
    default void velocity() {
        Table table = a().table();
        DPoint va = a().solVelocity();
        DPoint vb = b().solVelocity();
        if (equals(table.collision())) {
            DPoint na = b().solPosition().minus(a().solPosition()).normal();
            DPoint nb = na.mult(-1.0);
            DPoint vna = na.mult(va.dot(na));
            DPoint vnb = nb.mult(vb.dot(nb));
            DPoint vta = va.minus(vna);
            DPoint vtb = vb.minus(vnb);
            double f = 1.0 - table.ballsBouncingResistance();
            set(this, BallBallPair::aVelocity, vta.plus(vnb).mult(f));
            set(this, BallBallPair::bVelocity, vtb.plus(vna).mult(f));
        } else {
            set(this, BallBallPair::aVelocity, va);
            set(this, BallBallPair::bVelocity, vb);
        }
    }

    @Override
    default double distance() {
        DPoint pa = a().solPosition();
        DPoint pb = b().solPosition();
        int radius = a().radius();
        return Math.abs(Math.abs(pb.minus(pa).length()) - radius - radius);
    }

}
