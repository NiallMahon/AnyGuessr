import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { startQuiz, submitGuess } from "../services/gameplayAPI";
import GameResult from "../components/GameResult";
import QuizMap from "../components/Map/QuizMap";
import "./Quiz.css";

interface QuizLocationState {
  sessionId: string;
  mapId: string;
  initialTarget: string;
  targets: string[];
}

interface Progress {
  currentTarget: string;
  completed: string[];
  correctAnswers: number;
  answeredTargets: number;
  correctionRequired: boolean;
  complete: boolean;
}

// Mirrors the backend QuizSession rules so feedback is instant.
function applyGuess(targets: string[], progress: Progress, selected: string): Progress {
  const correct = selected === progress.currentTarget;
  const firstTry = !progress.correctionRequired;
  const scored = {
    answeredTargets: progress.answeredTargets + (firstTry ? 1 : 0),
    correctAnswers: progress.correctAnswers + (firstTry && correct ? 1 : 0),
  };

  if (!correct) {
    return { ...progress, ...scored, correctionRequired: true };
  }

  const completed = [...progress.completed, progress.currentTarget];
  const next = targets.find((target) => !completed.includes(target));

  return {
    ...scored,
    completed,
    currentTarget: next ?? progress.currentTarget,
    correctionRequired: false,
    complete: next === undefined,
  };
}

function Quiz() {
  const location = useLocation();
  const navigate = useNavigate();
  const quizState = location.state as QuizLocationState | null;

  const [progress, setProgress] = useState<Progress>({
    currentTarget: quizState?.initialTarget ?? "",
    completed: [],
    correctAnswers: 0,
    answeredTargets: 0,
    correctionRequired: false,
    complete: false,
  });
  const [message, setMessage] = useState("");
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

  const { sessionId, mapId, targets } = quizState;
  const { complete, correctAnswers, answeredTargets } = progress;
  const accuracy =
    answeredTargets === 0 ? 0 : (correctAnswers * 100) / answeredTargets;

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
          targets: quiz.targets,
        },
      });
    } catch (err) {
      console.error("Unable to restart the quiz:", err);
      setError("Unable to start another quiz. Please try again.");
      setRestarting(false);
    }
  }

  function handleGuess(regionId: string) {
    if (complete) return;

    const next = applyGuess(targets, progress, regionId);
    submitGuess(sessionId, regionId);
    setProgress(next);

    if (next.correctionRequired) {
      setMessage("Not quite. Select the requested region to continue.");
    } else if (progress.correctionRequired) {
      setMessage("Correction accepted. Moving to the next target.");
    } else {
      setMessage("Correct! Well done.");
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
              </div>

              <div className="quiz-map-panel">
                <QuizMap
                  key={mapId}
                  mapId={mapId}
                  targetRegionId={progress.currentTarget}
                  disabled={false}
                  onRegionClick={handleGuess}
                />
              </div>
            </section>

            {message && (
              <div
                className={`quiz-feedback ${
                  progress.correctionRequired
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

function QuizPage() {
  const location = useLocation();
  return <Quiz key={location.key} />;
}

export default QuizPage;
