/**
 * Conway's Game of Life — subtle background canvas + button activity overlay.
 *
 * Ported from AssetMantle/website components/games/GlobalGameCanvas.js
 * (React) to vanilla JS for the Scala/Play explorer. The simulation logic
 * (sparse Map<rc,bool> grid, RAF tick with frameDelay throttle, B3/S23
 * neighbour rule) is identical; only the React lifecycle is replaced with
 * plain DOM hooks.
 *
 * Two entry points:
 *
 *   1. AssetMantleGameOfLife.mountBackground(target, opts)
 *      Page-wide canvas, fixed inset:0, behind everything. Seeds with a
 *      calm period-26 reflector loop pattern and runs at low opacity.
 *
 *   2. AssetMantleGameOfLife.runOnButton(buttonEl, opts)
 *      Overlays a small canvas behind the button label while the button
 *      is in a busy state. Calling .stop() on the returned handle freezes
 *      the simulation so the button can return to its normal style.
 *      Handy for confirm / loading flows.
 */
(function (global) {
  "use strict";

  // Period-26 dependent reflector loop (Matthias Merzenich, 2013) - the calmest
  // life-ish loop we have; pulses on a 26-tick cycle so it never clutters.
  // Source: conwaylife.com/wiki/P26_dependent_reflector_loop
  var P26_REFLECTOR =
    "..........OO\n" +
    "..........O\n" +
    "....OO.OO.O....OO\n" +
    "..O..O.O.O.....O\n" +
    "..OO....O........O\n" +
    "................OO\n" +
    "\n" +
    "................OO\n" +
    "O..........O...O.O\n" +
    "OOO.......OOO...O\n" +
    "...O......O.O....OOO\n" +
    "..O.O..............O\n" +
    "..OO\n" +
    "\n" +
    "..OO\n" +
    "..O........O....OO\n" +
    "....O.....O.O.O..O\n" +
    "...OO....O.OO.OO\n" +
    ".........O\n" +
    "........OO";

  // Gosper glider gun - classic, generates a small stream. Used for buttons
  // because the constant motion reads as "working...".
  var GOSPER_GUN =
    "........................O...........\n" +
    "......................O.O...........\n" +
    "............OO......OO............OO\n" +
    "...........O...O....OO............OO\n" +
    "OO........O.....O...OO\n" +
    "OO........O...O.OO....O.O\n" +
    "..........O.....O.......O\n" +
    "...........O...O\n" +
    "............OO";

  function parsePattern(text) {
    var lines = text.split("\n");
    var maxLen = 0;
    for (var i = 0; i < lines.length; i++) if (lines[i].length > maxLen) maxLen = lines[i].length;
    return { lines: lines, width: maxLen, height: lines.length };
  }

  /**
   * Core simulator. Sparse grid stored as { "r,c": true }.
   * @param {HTMLCanvasElement} canvas
   * @param {{ cellSize:number, frameDelay:number, cellColor:string, gridColor:?string,
   *           pattern:string, place:(cols,rows,w,h)=>{x:number,y:number} }} opts
   */
  function Simulation(canvas, opts) {
    var ctx = canvas.getContext("2d");
    var grid = Object.create(null);
    var running = false;
    var lastTick = 0;
    var rafId = 0;
    var rows = 0, cols = 0;
    var cellSize = opts.cellSize;
    var frameDelay = opts.frameDelay;
    var cellColor = opts.cellColor;
    var gridColor = opts.gridColor || null;
    var parsed = parsePattern(opts.pattern);
    var place = opts.place || function (c, r, w, h) {
      return { x: Math.floor((c - w) / 2), y: Math.floor((r - h) / 2) };
    };

    function fitCanvas() {
      var dpr = global.devicePixelRatio || 1;
      var rect = canvas.getBoundingClientRect();
      cols = Math.floor(rect.width / cellSize);
      rows = Math.floor(rect.height / cellSize);
      // Defensive: avoid zero-sized canvases (e.g. display:none parents)
      if (cols < 1) cols = 1;
      if (rows < 1) rows = 1;
      canvas.width = cols * cellSize * dpr;
      canvas.height = rows * cellSize * dpr;
      canvas.style.width = cols * cellSize + "px";
      canvas.style.height = rows * cellSize + "px";
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }

    function seed() {
      grid = Object.create(null);
      var origin = place(cols, rows, parsed.width, parsed.height);
      for (var r = 0; r < parsed.lines.length; r++) {
        var line = parsed.lines[r];
        for (var c = 0; c < line.length; c++) {
          if (line.charAt(c) === "O") {
            grid[(origin.y + r) + "," + (origin.x + c)] = true;
          }
        }
      }
    }

    function neighbours(r, c) {
      var n = 0;
      for (var i = -1; i <= 1; i++) {
        for (var j = -1; j <= 1; j++) {
          if (i === 0 && j === 0) continue;
          if (grid[(r + i) + "," + (c + j)]) n++;
        }
      }
      return n;
    }

    function step() {
      var next = Object.create(null);
      var consider = Object.create(null);
      for (var key in grid) {
        var parts = key.split(",");
        var r = +parts[0], c = +parts[1];
        for (var i = -1; i <= 1; i++) for (var j = -1; j <= 1; j++) consider[(r + i) + "," + (c + j)] = true;
      }
      for (var k in consider) {
        var p = k.split(",");
        var rr = +p[0], cc = +p[1];
        var n2 = neighbours(rr, cc);
        if (grid[k]) { if (n2 === 2 || n2 === 3) next[k] = true; }
        else if (n2 === 3) next[k] = true;
      }
      grid = next;
    }

    function draw() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      ctx.fillStyle = cellColor;
      for (var key in grid) {
        var p = key.split(",");
        var r = +p[0], c = +p[1];
        if (r < 0 || c < 0 || r >= rows || c >= cols) continue;
        ctx.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
      }
      if (gridColor) {
        ctx.strokeStyle = gridColor;
        ctx.lineWidth = 0.5;
        for (var rg = 0; rg < rows; rg++) {
          for (var cg = 0; cg < cols; cg++) {
            ctx.strokeRect(cg * cellSize, rg * cellSize, cellSize, cellSize);
          }
        }
      }
    }

    function loop(now) {
      if (!running) return;
      if (now - lastTick >= frameDelay) {
        step();
        draw();
        lastTick = now;
      }
      rafId = global.requestAnimationFrame(loop);
    }

    return {
      start: function () {
        if (running) return;
        fitCanvas();
        seed();
        draw();
        running = true;
        lastTick = global.performance.now();
        rafId = global.requestAnimationFrame(loop);
      },
      stop: function () {
        running = false;
        if (rafId) global.cancelAnimationFrame(rafId);
      },
      resize: function () {
        if (!running) return;
        fitCanvas();
        seed();
        draw();
      }
    };
  }

  function mountBackground(opts) {
    opts = opts || {};
    var existing = document.getElementById("am-game-bg");
    if (existing) return existing._sim;
    var canvas = document.createElement("canvas");
    canvas.id = "am-game-bg";
    canvas.style.position = "fixed";
    canvas.style.inset = "0";
    canvas.style.width = "100vw";
    canvas.style.height = "100vh";
    canvas.style.pointerEvents = "none";
    canvas.style.zIndex = "0";
    canvas.style.opacity = String(opts.opacity != null ? opts.opacity : 0.07);
    document.body.insertBefore(canvas, document.body.firstChild);

    var sim = Simulation(canvas, {
      cellSize: opts.cellSize || 6,
      frameDelay: opts.frameDelay || 90,
      cellColor: opts.cellColor || "#FBAB30",
      pattern: opts.pattern || P26_REFLECTOR,
      place: function (c, r, w, h) {
        return { x: Math.floor((c - w) / 2), y: Math.floor((r - h) / 2) };
      }
    });
    canvas._sim = sim;
    sim.start();

    var t;
    global.addEventListener("resize", function () {
      clearTimeout(t);
      t = setTimeout(function () { sim.resize(); }, 150);
    });
    return sim;
  }

  /**
   * Overlay a Gosper-gun life canvas behind a button while it's in a busy
   * state. Returns a handle with .stop() that freezes the canvas so the
   * caller can flip the button into a confirmed/idle look.
   */
  function runOnButton(button, opts) {
    if (!button) return null;
    opts = opts || {};
    if (button._gameOfLife) return button._gameOfLife;

    var rect = button.getBoundingClientRect();
    var canvas = document.createElement("canvas");
    canvas.style.position = "absolute";
    canvas.style.inset = "0";
    canvas.style.width = "100%";
    canvas.style.height = "100%";
    canvas.style.pointerEvents = "none";
    canvas.style.opacity = String(opts.opacity != null ? opts.opacity : 0.45);
    canvas.style.zIndex = "0";

    // Make sure children render above the canvas
    var prevPosition = button.style.position;
    if (!prevPosition || prevPosition === "static") button.style.position = "relative";
    button.appendChild(canvas);
    var children = button.children;
    for (var i = 0; i < children.length; i++) {
      if (children[i] !== canvas && !children[i].style.zIndex) children[i].style.zIndex = "1";
      if (children[i] !== canvas && !children[i].style.position) children[i].style.position = "relative";
    }
    button.classList.add("am-game-button");

    var sim = Simulation(canvas, {
      cellSize: opts.cellSize || 3,
      frameDelay: opts.frameDelay || 60,
      cellColor: opts.cellColor || "#FBAB30",
      pattern: opts.pattern || GOSPER_GUN,
      place: function (c, r, w, h) {
        return { x: 1, y: Math.max(0, Math.floor((r - h) / 2)) };
      }
    });
    sim.start();

    var handle = {
      stop: function () {
        sim.stop();
        if (canvas.parentNode === button) button.removeChild(canvas);
        button.classList.remove("am-game-button");
        if (!prevPosition) button.style.position = "";
        delete button._gameOfLife;
      }
    };
    button._gameOfLife = handle;
    return handle;
  }

  global.AssetMantleGameOfLife = {
    mountBackground: mountBackground,
    runOnButton: runOnButton,
    Simulation: Simulation,
    patterns: { P26_REFLECTOR: P26_REFLECTOR, GOSPER_GUN: GOSPER_GUN }
  };
})(window);
