import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import {
  startQuiz,
  submitGuess,
  type GuessResponse,
} from "../services/gameplayAPI";
import GameResult from "../components/GameResult";
import QuizMap from "../components/Map/QuizMap";
import "./Quiz.css";

/** Session details passed from the Map Selector through React Router. */
interface QuizLocationState {
  sessionId: string;
  mapId: string;
  initialTarget: string;
}

/**
 * Presents the playable polygon map and quiz progress.
 * The backend remains responsible for correctness, scoring, and correction rules.
 */
function Quiz() {
  const location = useLocation();
  const navigate = useNavigate();
  const quizState = location.state as QuizLocationState | null;

  const [currentTarget, setCurrentTarget] = useState(
    quizState?.initialTarget ?? ""
  );
  const [lastResult, setLastResult] = useState<GuessResponse | null>(null);
  const [message, setMessage] = useState("");
  const [sending, setSending] = useState(false);
  const [restarting, setRestarting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!quizState) {
    return (
      <main className="quiz-page">
        <section className="quiz-empty-card">
          <p className="quiz-eyebrow">ANYGUESSR / PLAY</p>
          <h1>No active quiz</h1>
          <p>Choose a map to start a new quiz.</p>
          <button
            type="button"
            className="quiz-action quiz-action--primary"
            onClick={() => navigate("/mapselector")}
          >
            Choose a map
          </button>
        </section>
      </main>
    );
  }

  const { sessionId, mapId } = quizState;
  const complete = lastResult?.quizComplete ?? false;
  const correctAnswers = lastResult?.correctAnswers ?? 0;
  const answeredTargets = lastResult?.answeredTargets ?? 0;
  const accuracy = lastResult?.accuracy ?? 0;

  /** Starts a fresh quiz on the same map, without resetting domain rules locally. */
  async function playAgain() {
    if (restarting) return;
    setRestarting(true);
    setError(null);

    const storedUserId = sessionStorage.getItem("userId");

    try {
      const quiz = await startQuiz(
        mapId,
        storedUserId === null ? null : Number(storedUserId)
      );

      navigate("/quiz", {
        state: {
          sessionId: quiz.sessionId,
          mapId: quiz.mapId,
          initialTarget: quiz.currentTarget,
        },
      });
    } catch (err) {
      console.error("Unable to restart the quiz:", err);
      setError("Unable to start another quiz. Please try again.");
      setRestarting(false);
    }
  }

  /** Submits the selected polygon and uses the backend response as source of truth. */
  async function handleGuess(regionId: string) {
    if (sending || complete) return;

    const correcting = lastResult?.correctionRequired ?? false;
    setSending(true);
    setError(null);

    try {
      const result = await submitGuess(sessionId, regionId);
      setLastResult(result);

      if (result.quizComplete) {
        setMessage("Quiz complete!");
        return;
      }

      setCurrentTarget(result.currentTargetRegionId);

      if (correcting && result.correct) {
        setMessage("Correction accepted. Moving to the next target.");
      } else if (result.correct) {
        setMessage("Correct! Well done.");
      } else if (result.correctionRequired) {
        setMessage("Not quite. Select the requested region to continue.");
      } else {
        setMessage("Selection recorded.");
      }
    } catch (err) {
      console.error("Unable to submit guess:", err);
      setError("Unable to submit your guess. Please try again.");
    } finally {
      setSending(false);
    }
  }

  return (
    <main className="quiz-page">
      <div className="quiz-shell">
        <header className="quiz-header">
          <p className="quiz-eyebrow">ANYGUESSR / PLAY</p>
          <h1>Core Quiz Gameplay</h1>
          <p className="quiz-subtitle">Map {mapId}</p>
        </header>

        {!complete ? (
          <>
            <section className="quiz-board-card" aria-label="Current quiz">
              <div className="quiz-stage-header">
                <div>
                  <p className="quiz-stage-label">YOUR CHALLENGE</p>
                  <p>Click the region you think is correct.</p>
                </div>
                {sending && (
                  <span className="quiz-submitting" role="status">
                    Checking answer…
                  </span>
                )}
              </div>

              <div className="quiz-map-panel">
                <QuizMap
                  key={mapId}
                  mapId={mapId}
                  targetRegionId={currentTarget}
                  disabled={sending}
                  onRegionClick={(regionId) => {
                    void handleGuess(regionId);
                  }}
                />
              </div>
            </section>

            {message && (
              <div
                className={`quiz-feedback ${
                  lastResult?.correctionRequired
                    ? "quiz-feedback--warning"
                    : "quiz-feedback--success"
                }`}
                role="status"
                aria-live="polite"
              >
                {message}
              </div>
            )}

            <div className="quiz-score-grid" aria-label="Current quiz score">
              <div className="quiz-score-tile">
                <span>Correct answers</span>
                <strong>{correctAnswers}</strong>
              </div>
              <div className="quiz-score-tile">
                <span>Answered targets</span>
                <strong>{answeredTargets}</strong>
              </div>
              <div className="quiz-score-tile">
                <span>Accuracy</span>
                <strong>{accuracy.toFixed(1)}%</strong>
              </div>
            </div>
          </>
        ) : (
          <section className="quiz-complete-card" aria-label="Quiz results">
            <div className="quiz-completion-mark" aria-hidden="true">✓</div>
            <p className="quiz-eyebrow">SESSION FINISHED</p>
            <h2>Quiz complete!</h2>
            <p className="quiz-complete-description">
              You finished the map. Here's how you did.
            </p>

            <p className="quiz-final-accuracy">{accuracy.toFixed(1)}%</p>
            <p className="quiz-final-caption">Final accuracy</p>

            <div className="quiz-score-grid" aria-label="Final quiz score">
              <div className="quiz-score-tile">
                <span>Correct answers</span>
                <strong>{correctAnswers}</strong>
              </div>
              <div className="quiz-score-tile">
                <span>Answered targets</span>
                <strong>{answeredTargets}</strong>
              </div>
              <div className="quiz-score-tile">
                <span>Accuracy</span>
                <strong>{accuracy.toFixed(1)}%</strong>
              </div>
            </div>

            {sessionStorage.getItem("userId") !== null && (
              <GameResult sessionId={sessionId} />
            )}

            <div className="quiz-actions">
              <button
                type="button"
                className="quiz-action quiz-action--primary"
                disabled={restarting}
                onClick={() => void playAgain()}
              >
                {restarting ? "Starting…" : "Play again"}
              </button>
              <button
                type="button"
                className="quiz-action quiz-action--secondary"
                onClick={() => navigate("/mapselector")}
              >
                Choose another map
              </button>
            </div>
          </section>
        )}

        {error && (
          <p className="quiz-error" role="alert">
            {error}
          </p>
        )}
      </div>
    </main>
  );
}

/** Remounts the quiz when navigation starts a new gameplay session. */
function QuizPage() {
  const location = useLocation();
  return <Quiz key={location.key} />;
}

export default QuizPage;
