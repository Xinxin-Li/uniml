package edu.hitsz.optimization.lbfgs.cmu;

/* Mcsrch.java
 * Copyright (C) 1999, Robert Dodier (robert_dodier@yahoo.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2.1 of the GNU Lesser General Public License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA, 02111-1307, USA,
 * or visit the GNU web site, www.gnu.org.
 */

/**
 * This class implements an algorithm for multi-dimensional line search. This
 * file is a translation of Fortran code written by Jorge Nocedal. See comments
 * in the file <tt>LBFGS.java</tt> for more information.
 */
public class Mcsrch {
        private static int infoc[] = new int[1], j = 0;
        private static double dg = 0, dgm = 0, dginit = 0, dgtest = 0,
                        dgx[] = new double[1], dgxm[] = new double[1],
                        dgy[] = new double[1], dgym[] = new double[1], finit = 0,
                        ftest1 = 0, fm = 0, fx[] = new double[1], fxm[] = new double[1],
                        fy[] = new double[1], fym[] = new double[1];
        static double stx[] = new double[1];
        static double sty[] = new double[1];
        static double stmin = 0, stmax = 0, width = 0, width1 = 0;
        static boolean brackt[] = new boolean[1];
        static boolean stage1 = false;
        private static final double ONE_HALF = 0.5, TWO_THIRDS = 0.66, FOUR = 4;

        static double sqr(double x) {
                return x * x;
        }

        static double max3(double x, double y, double z) {
                return x < y ? (y < z ? z : y) : (x < z ? z : x);
        }

        /**
         * Minimize a function along a search direction. This code is a Java
         * translation of the function <code>MCSRCH</code> from <code>lbfgs.f</code>
         * , which in turn is a slight modification of the subroutine
         * <code>CSRCH</code> of More' and Thuente. The changes are to allow reverse
         * communication, and do not affect the performance of the routine. This
         * function, in turn, calls <code>mcstep</code>.
         * <p>
         *
         * The Java translation was effected mostly mechanically, with some manual
         * clean-up; in particular, array indices start at 0 instead of 1. Most of
         * the comments from the Fortran code have been pasted in here as well.
         * <p>
         *
         * The purpose of <code>mcsrch</code> is to find a step which satisfies a
         * sufficient decrease condition and a curvature condition.
         * <p>
         *
         * At each stage this function updates an interval of uncertainty with
         * endpoints <code>stx</code> and <code>sty</code>. The interval of
         * uncertainty is initially chosen so that it contains a minimizer of the
         * modified function
         *
         * <pre>
         *      f(x+stp*s) - f(x) - ftol*stp*(gradf(x)'s).
         * </pre>
         *
         * If a step is obtained for which the modified function has a nonpositive
         * function value and nonnegative derivative, then the interval of
         * uncertainty is chosen so that it contains a minimizer of
         * <code>f(x+stp*s)</code>.
         * <p>
         *
         * The algorithm is designed to find a step which satisfies the sufficient
         * decrease condition
         *
         * <pre>
         *       f(x+stp*s) &lt;= f(X) + ftol*stp*(gradf(x)'s),
         * </pre>
         *
         * and the curvature condition
         *
         * <pre>
         *       abs(gradf(x+stp*s)'s)) &lt;= gtol*abs(gradf(x)'s).
         * </pre>
         *
         * If <code>ftol</code> is less than <code>gtol</code> and if, for example,
         * the function is bounded below, then there is always a step which
         * satisfies both conditions. If no step can be found which satisfies both
         * conditions, then the algorithm usually stops when rounding errors prevent
         * further progress. In this case <code>stp</code> only satisfies the
         * sufficient decrease condition.
         * <p>
         *
         * @author Original Fortran version by Jorge J. More' and David J. Thuente
         *         as part of the Minpack project, June 1983, Argonne National
         *         Laboratory. Java translation by Robert Dodier, August 1997.
         *
         * @param n
         *            The number of variables.
         *
         * @param x
         *            On entry this contains the base point for the line search. On
         *            exit it contains <code>x + stp*s</code>.
         *
         * @param f
         *            On entry this contains the value of the objective function at
         *            <code>x</code>. On exit it contains the value of the objective
         *            function at <code>x + stp*s</code>.
         *
         * @param g
         *            On entry this contains the gradient of the objective function
         *            at <code>x</code>. On exit it contains the gradient at
         *            <code>x + stp*s</code>.
         *
         * @param s
         *            The search direction.
         *
         * @param stp
         *            On entry this contains an initial estimate of a satifactory
         *            step length. On exit <code>stp</code> contains the final
         *            estimate.
         *
         * @param ftol
         *            Tolerance for the sufficient decrease condition.
         *
         * @param xtol
         *            Termination occurs when the relative width of the interval of
         *            uncertainty is at most <code>xtol</code>.
         *
         * @param maxfev
         *            Termination occurs when the number of evaluations of the
         *            objective function is at least <code>maxfev</code> by the end
         *            of an iteration.
         *
         * @param info
         *            This is an output variable, which can have these values:
         *            <ul>
         *            <li><code>info = 0</code> Improper input parameters. <li>
         *            <code>info = -1</code> A return is made to compute the
         *            function and gradient. <li><code>info = 1</code> The
         *            sufficient decrease condition and the directional derivative
         *            condition hold. <li><code>info = 2</code> Relative width of
         *            the interval of uncertainty is at most <code>xtol</code>. <li>
         *            <code>info = 3</code> Number of function evaluations has
         *            reached <code>maxfev</code>. <li><code>info = 4</code> The
         *            step is at the lower bound <code>stpmin</code>. <li><code>info
         *            = 5</code> The step is at the upper bound <code>stpmax</code>.
         *            <li><code>info = 6</code> Rounding errors prevent further
         *            progress. There may not be a step which satisfies the
         *            sufficient decrease and curvature conditions. Tolerances may
         *            be too small.
         *            </ul>
         *
         * @param nfev
         *            On exit, this is set to the number of function evaluations.
         *
         * @param wa
         *            Temporary storage array, of length <code>n</code>.
         */

        public static void mcsrch(final int n, double[] x, double f, double[] g,
                        double[] s, int is0, double[] stp, double ftol, double xtol,
                        int maxfev, int[] info, int[] nfev, double[] wa, int[] iprint) {
//              System.err.println("mc " + f + " " + stp[0]);

                if (info[0] != -1) {
                        infoc[0] = 1;
                        if (n <= 0 || stp[0] <= 0 || ftol < 0 || LBFGS.gtol < 0 || xtol < 0
                                        || LBFGS.stpmin < 0 || LBFGS.stpmax < LBFGS.stpmin
                                        || maxfev <= 0)
                                return;

                        // Compute the initial gradient in the search direction
                        // and check that s is a descent direction.
                        dginit = 0;
                        for (j = 1; j <= n; j += 1) {
                                dginit += g[j - 1] * s[is0 + j - 1];
                                if (Double.isNaN(dginit))
                                        System.err
                                                        .println("NaN " + g[j - 1] + " " + s[is0 + j - 1]);
                        }
                        if (dginit >= 0) {
                                System.out
                                                .println("The search direction is not a descent direction.");
                                return;
                        }

                        brackt[0] = false;
                        stage1 = true;
                        nfev[0] = 0;
                        finit = f;
                        dgtest = ftol * dginit;
                        width = LBFGS.stpmax - LBFGS.stpmin;
                        width1 = width / ONE_HALF;

                        for (j = 1; j <= n; j += 1) {
                                wa[j - 1] = x[j - 1];
                        }

                        // The variables stx, fx, dgx contain the values of the step,
                        // function, and directional derivative at the best step.
                        // The variables sty, fy, dgy contain the value of the step,
                        // function, and derivative at the other endpoint of
                        // the interval of uncertainty.
                        // The variables stp, f, dg contain the values of the step,
                        // function, and derivative at the current step.
                        stx[0] = 0;
                        fx[0] = finit;
                        dgx[0] = dginit;
                        sty[0] = 0;
                        fy[0] = finit;
                        dgy[0] = dginit;

                        if (iprint[0] > 0)
                                System.err.println("new line search f=" + finit + " dg="
                                                + dginit + " stp=" + stp[0]);
                }

                while (true) {
                        if (info[0] != -1) {
                                // Set the minimum and maximum steps to correspond
                                // to the present interval of uncertainty.
                                if (brackt[0]) {
                                        stmin = Math.min(stx[0], sty[0]);
                                        stmax = Math.max(stx[0], sty[0]);
                                } else {
                                        stmin = stx[0];
                                        stmax = stp[0] + FOUR * (stp[0] - stx[0]);
                                }

                                // Force the step to be within the bounds stpmax and stpmin.
                                setSTP(stp, Math.max(stp[0], LBFGS.stpmin));
                                setSTP(stp, Math.min(stp[0], LBFGS.stpmax));

                                // If an unusual termination is to occur then let
                                // stp be the lowest point obtained so far.
                                if ((brackt[0] && (stp[0] <= stmin || stp[0] >= stmax || stmax
                                                - stmin <= xtol * stmax))
                                                || nfev[0] >= maxfev - 1 || infoc[0] == 0)
                                        setSTP(stp, stx[0]);

                                // Evaluate the function and gradient at stp
                                // and compute the directional derivative.
                                // We return to main program to obtain F and G.
                                for (j = 1; j <= n; j += 1) {
                                        x[j - 1] = wa[j - 1] + stp[0] * s[is0 + j - 1];
                                        if (Math.abs(x[j - 1]) > 40) {
                                                System.err.println("big wt " + j + " " + stp[0] + " "
                                                                + s[is0 + j - 1]);
                                        }
                                }

                                info[0] = -1;
                                return;
                        }

                        info[0] = 0;
                        nfev[0] = nfev[0] + 1;
                        dg = 0;

                        for (j = 1; j <= n; j += 1) {
                                dg = dg + g[j - 1] * s[is0 + j - 1];
                        }

                        ftest1 = finit + stp[0] * dgtest;

                        // Test for convergence.
                        if ((brackt[0] && (stp[0] <= stmin || stp[0] >= stmax))
                                        || infoc[0] == 0)
                                // Rounding errors prevent further
                                // progress. There may not be a step which satisfies the
                                // sufficient decrease and curvature conditions. Tolerances
                                // may
                                // be too small.
                                info[0] = 6;

                        if (stp[0] == LBFGS.stpmax && f <= ftest1 && dg <= dgtest)
                                // The step is at the upper bound <code>stpmax</code>.
                                info[0] = 5;

                        if (stp[0] == LBFGS.stpmin && (f > ftest1 || dg >= dgtest))
                                // The step is at the lower bound <code>stpmin</code>.
                                info[0] = 4;

                        if (nfev[0] >= maxfev)
                                // Number of function evaluations has reached
                                // <code>maxfev</code>
                                info[0] = 3;

                        if (brackt[0] && stmax - stmin <= xtol * stmax)
                                // Relative width of the interval of uncertainty is at most
                                // <code>xtol</code>
                                info[0] = 2;

                        if (f <= ftest1 && Math.abs(dg) <= LBFGS.gtol * (-dginit))
                                // The sufficient decrease condition and the directional
                                // derivative condition hold.
                                info[0] = 1;

                        // Check for termination.
                        if (info[0] != 0) {
                                info[0] = 1;
                                return;
                        }

                        // In the first stage we seek a step for which the modified
                        // function has a nonpositive value and nonnegative derivative.
                        if (stage1 && f <= ftest1
                                        && dg >= Math.min(ftol, LBFGS.gtol) * dginit)
                                stage1 = false;

                        // A modified function is used to predict the step only if
                        // we have not obtained a step for which the modified
                        // function has a nonpositive function value and nonnegative
                        // derivative, and if a lower function value has been
                        // obtained but the decrease is not sufficient.
                        if (stage1 && f <= fx[0] && f > ftest1) {
                                // Define the modified function and derivative values.
                                fm = f - stp[0] * dgtest;
                                fxm[0] = fx[0] - stx[0] * dgtest;
                                fym[0] = fy[0] - sty[0] * dgtest;
                                dgm = dg - dgtest;
                                dgxm[0] = dgx[0] - dgtest;
                                dgym[0] = dgy[0] - dgtest;

                                // Call cstep to update the interval of uncertainty
                                // and to compute the new step.
                                mcstep(stx, fxm, dgxm, sty, fym, dgym, stp, fm, dgm, brackt,
                                                stmin, stmax, infoc, iprint);

                                // Reset the function and gradient values for f.
                                fx[0] = fxm[0] + stx[0] * dgtest;
                                fy[0] = fym[0] + sty[0] * dgtest;
                                dgx[0] = dgxm[0] + dgtest;
                                dgy[0] = dgym[0] + dgtest;
                        } else {
                                // Call mcstep to update the interval of uncertainty
                                // and to compute the new step.
                                mcstep(stx, fx, dgx, sty, fy, dgy, stp, f, dg, brackt, stmin,
                                                stmax, infoc, iprint);
                        }

                        if (iprint[0] > 0)
                                System.err.println(" msrch internal f=" + f + " dg=" + dg
                                                + " stx=" + stx[0] + " sty=" + sty[0] + " brackt="
                                                + brackt[0] + " stp=" + stp[0]);

                        // Force a sufficient decrease in the size of the
                        // interval of uncertainty.
                        if (brackt[0]) {
                                if (Math.abs(sty[0] - stx[0]) >= TWO_THIRDS * width1)
                                        setSTP(stp, (sty[0] + stx[0]) / 2.0);
                                width1 = width;
                                width = Math.abs(sty[0] - stx[0]);
                        }
                }
        }

        /**
         * The purpose of this function is to compute a safeguarded step for a
         * linesearch and to update an interval of uncertainty for a minimizer of
         * the function.
         * <p>
         *
         * The parameter <code>stx</code> contains the step with the least function
         * value. The parameter <code>stp</code> contains the current step. It is
         * assumed that the derivative at <code>stx</code> is negative in the
         * direction of the step. If <code>brackt[0]</code> is <code>true</code>
         * when <code>mcstep</code> returns then a minimizer has been bracketed in
         * an interval of uncertainty with endpoints <code>stx</code> and
         * <code>sty</code>.
         * <p>
         *
         * Variables that must be modified by <code>mcstep</code> are implemented as
         * 1-element arrays.
         *
         * @param stx1
         *            Step at the best step obtained so far. This variable is
         *            modified by <code>mcstep</code>.
         * @param fx1
         *            Function value at the best step obtained so far. This variable
         *            is modified by <code>mcstep</code>.
         * @param dx
         *            Derivative at the best step obtained so far. The derivative
         *            must be negative in the direction of the step, that is,
         *            <code>dx</code> and <code>stp-stx</code> must have opposite
         *            signs. This variable is modified by <code>mcstep</code>.
         *
         * @param sty1
         *            Step at the other endpoint of the interval of uncertainty.
         *            This variable is modified by <code>mcstep</code>.
         * @param fy1
         *            Function value at the other endpoint of the interval of
         *            uncertainty. This variable is modified by <code>mcstep</code>.
         * @param dy
         *            Derivative at the other endpoint of the interval of
         *            uncertainty. This variable is modified by <code>mcstep</code>.
         *
         * @param stp
         *            Step at the current step. If <code>brackt</code> is set then
         *            on input <code>stp</code> must be between <code>stx</code> and
         *            <code>sty</code>. On output <code>stp</code> is set to the new
         *            step.
         * @param fp
         *            Function value at the current step.
         * @param dp
         *            Derivative at the current step.
         *
         * @param brackt1
         *            Tells whether a minimizer has been bracketed. If the minimizer
         *            has not been bracketed, then on input this variable must be
         *            set <code>false</code>. If the minimizer has been bracketed,
         *            then on output this variable is <code>true</code>.
         *
         * @param stpmin
         *            Lower bound for the step.
         * @param stpmax
         *            Upper bound for the step.
         *
         * @param info
         *            On return from <code>mcstep</code>, this is set as follows: If
         *            <code>info</code> is 1, 2, 3, or 4, then the step has been
         *            computed successfully. Otherwise <code>info</code> = 0, and
         *            this indicates improper input parameters.
         *
         * @author Jorge J. More, David J. Thuente: original Fortran version, as
         *         part of Minpack project. Argonne Nat'l Laboratory, June 1983.
         *         Robert Dodier: Java translation, August 1997.
         */
        public static void mcstep(double[] stx1, double[] fx1, double[] dx,
                        double[] sty1, double[] fy1, double[] dy, double[] stp, double fp,
                        double dp, boolean[] brackt1, double stpmin, double stpmax,
                        int[] info, int[] iprint) {
                boolean bound;
                double gamma, p, q, r, s, sgnd, stpc, stpf, stpq, theta;

                info[0] = 0;

                if ((brackt1[0] && (stp[0] <= Math.min(stx1[0], sty1[0]) || stp[0] >= Math
                                .max(stx1[0], sty1[0])))
                                || dx[0] * (stp[0] - stx1[0]) >= 0.0 || stpmax < stpmin) {
                        if (iprint[0] > 0)
                                System.err.println("mcstep=0 " + brackt1[0] + " " + stp[0]
                                                + " " + stx1[0] + " " + sty1[0] + " " + dx[0] + " "
                                                + stpmax + " " + stpmin);
                        return;
                }

                // Determine if the derivatives have opposite sign.

                sgnd = dp * (dx[0] / Math.abs(dx[0]));

                if (fp > fx1[0]) {
                        // First case. A higher function value.
                        // The minimum is bracketed. If the cubic step is closer
                        // to stx than the quadratic step, the cubic step is taken,
                        // else the average of the cubic and quadratic steps is taken.

                        info[0] = 1;
                        bound = true;
                        theta = 3 * (fx1[0] - fp) / (stp[0] - stx1[0]) + dx[0] + dp;
                        s = max3(Math.abs(theta), Math.abs(dx[0]), Math.abs(dp));
                        gamma = s * Math.sqrt(sqr(theta / s) - (dx[0] / s) * (dp / s));
                        if (stp[0] < stx1[0])
                                gamma = -gamma;
                        p = (gamma - dx[0]) + theta;
                        q = ((gamma - dx[0]) + gamma) + dp;
                        r = p / q;
                        stpc = stx1[0] + r * (stp[0] - stx1[0]);
                        stpq = stx1[0]
                                        + ((dx[0] / ((fx1[0] - fp) / (stp[0] - stx1[0]) + dx[0])) / 2)
                                        * (stp[0] - stx1[0]);
                        if (Math.abs(stpc - stx1[0]) < Math.abs(stpq - stx1[0])) {
                                stpf = stpc;
                        } else {
                                stpf = stpc + (stpq - stpc) / 2;
                        }
                        brackt1[0] = true;
                } else if (sgnd < 0.0) {
                        // Second case. A lower function value and derivatives of
                        // opposite sign. The minimum is bracketed. If the cubic
                        // step is closer to stx than the quadratic (secant) step,
                        // the cubic step is taken, else the quadratic step is taken.

                        info[0] = 2;
                        bound = false;
                        theta = 3 * (fx1[0] - fp) / (stp[0] - stx1[0]) + dx[0] + dp;
                        s = max3(Math.abs(theta), Math.abs(dx[0]), Math.abs(dp));
                        gamma = s * Math.sqrt(sqr(theta / s) - (dx[0] / s) * (dp / s));
                        if (stp[0] > stx1[0])
                                gamma = -gamma;
                        p = (gamma - dp) + theta;
                        q = ((gamma - dp) + gamma) + dx[0];
                        r = p / q;
                        stpc = stp[0] + r * (stx1[0] - stp[0]);
                        stpq = stp[0] + (dp / (dp - dx[0])) * (stx1[0] - stp[0]);
                        if (Math.abs(stpc - stp[0]) > Math.abs(stpq - stp[0])) {
                                stpf = stpc;
                        } else {
                                stpf = stpq;
                        }
                        brackt1[0] = true;
                } else if (Math.abs(dp) < Math.abs(dx[0])) {
                        // Third case. A lower function value, derivatives of the
                        // same sign, and the magnitude of the derivative decreases.
                        // The cubic step is only used if the cubic tends to infinity
                        // in the direction of the step or if the minimum of the cubic
                        // is beyond stp. Otherwise the cubic step is defined to be
                        // either stpmin or stpmax. The quadratic (secant) step is also
                        // computed and if the minimum is bracketed then the the step
                        // closest to stx is taken, else the step farthest away is taken.

                        info[0] = 3;
                        bound = true;
                        theta = 3 * (fx1[0] - fp) / (stp[0] - stx1[0]) + dx[0] + dp;
                        s = max3(Math.abs(theta), Math.abs(dx[0]), Math.abs(dp));
                        gamma = s
                                        * Math.sqrt(Math.max(0, sqr(theta / s) - (dx[0] / s)
                                                        * (dp / s)));
                        if (stp[0] > stx1[0])
                                gamma = -gamma;
                        p = (gamma - dp) + theta;
                        q = (gamma + (dx[0] - dp)) + gamma;
                        r = p / q;
                        if (r < 0.0 && gamma != 0.0) {
                                stpc = stp[0] + r * (stx1[0] - stp[0]);
                        } else if (stp[0] > stx1[0]) {
                                stpc = stpmax;
                        } else {
                                stpc = stpmin;
                        }
                        stpq = stp[0] + (dp / (dp - dx[0])) * (stx1[0] - stp[0]);
                        if (brackt1[0]) {
                                if (Math.abs(stp[0] - stpc) < Math.abs(stp[0] - stpq)) {
                                        stpf = stpc;
                                } else {
                                        stpf = stpq;
                                }
                        } else {
                                if (Math.abs(stp[0] - stpc) > Math.abs(stp[0] - stpq)) {
                                        stpf = stpc;
                                } else {
                                        stpf = stpq;
                                }
                        }
                } else {
                        // Fourth case. A lower function value, derivatives of the
                        // same sign, and the magnitude of the derivative does
                        // not decrease. If the minimum is not bracketed, the step
                        // is either stpmin or stpmax, else the cubic step is taken.

                        info[0] = 4;
                        bound = false;
                        if (brackt1[0]) {
                                theta = 3 * (fp - fy1[0]) / (sty1[0] - stp[0]) + dy[0] + dp;
                                s = max3(Math.abs(theta), Math.abs(dy[0]), Math.abs(dp));
                                gamma = s * Math.sqrt(sqr(theta / s) - (dy[0] / s) * (dp / s));
                                if (stp[0] > sty1[0])
                                        gamma = -gamma;
                                p = (gamma - dp) + theta;
                                q = ((gamma - dp) + gamma) + dy[0];
                                r = p / q;
                                stpc = stp[0] + r * (sty1[0] - stp[0]);
                                stpf = stpc;
                        } else if (stp[0] > stx1[0]) {
                                stpf = stpmax;
                        } else {
                                stpf = stpmin;
                        }
                }

                // Update the interval of uncertainty. This update does not
                // depend on the new step or the case analysis above.

                if (fp > fx1[0]) {
                        sty1[0] = stp[0];
                        fy1[0] = fp;
                        dy[0] = dp;
                } else {
                        if (sgnd < 0.0) {
                                sty1[0] = stx1[0];
                                fy1[0] = fx1[0];
                                dy[0] = dx[0];
                        }
                        stx1[0] = stp[0];
                        fx1[0] = fp;
                        dx[0] = dp;
                }

                // Compute the new step and safeguard it.

                stpf = Math.min(stpmax, stpf);
                stpf = Math.max(stpmin, stpf);
                setSTP(stp, stpf);

                if (brackt1[0] && bound) {
                        double possibleStep = stx1[0] + TWO_THIRDS * (sty1[0] - stx1[0]);
                        if (sty1[0] > stx1[0]) {
                                setSTP(stp, Math.min(possibleStep, stp[0]));
                        } else {
                                setSTP(stp, Math.max(possibleStep, stp[0]));
                        }
                }
//              System.err.println("mcsetp stp => " + stp[0] + " info=" + info[0]);

                return;
        }

        static void setSTP(double[] stp, double value) {
                if (value < 0)// || value > 10)
                        throw new IllegalArgumentException(value + "");
                if (value > 1000) {
                        stp[0] = 1000;
                        System.err.println("Reducing step from " + value + " to 1000");
                } else {
                        stp[0] = value;
                }
                // curStp = stp[0];
        }
}

