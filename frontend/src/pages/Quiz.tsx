
import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import {
  startQuiz,
  submitGuess,
  type GuessResponse,
} from "../services/gameplayAPI";
import GameResult from "../components/GameResult";
import QuizMap from "../components/Map/QuizMap";

interface QuizLocationState {
  sessionId: string;
  mapId: string;
  initialTarget: string;
}

/**
 * Displays an active quiz and handles gameplay progress.
 *
 * Players select regions by clicking polygons on the map.
 * Guess evaluation and scoring are handled by the backend.
 */
function Quiz() {
  const location = useLocation();
  const navigate = useNavigate();

  const quizState =
    location.state as QuizLocationState | null;

  const [currentTarget, setCurrentTarget] =
    useState(quizState?.initialTarget ?? "");

  const [lastResult, setLastResult] =
    useState<GuessResponse | null>(null);

  const [message, setMessage] =
    useState("");

  const [sending, setSending] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  if (!quizState) {
    return (
      <div
        style={{
          padding: "2rem",
          textAlign: "center",
        }}
      >
        <h1>No active quiz</h1>

        <p>
          Start a quiz from the map selector.
        </p>

        <button
          onClick={() => navigate("/mapselector")}
        >
          Choose a map
        </button>
      </div>
    );
  }

  const sessionId = quizState.sessionId;
  const mapId = quizState.mapId;

  /**
   * Starts a new quiz using the same selected map.
   */
  async function playAgain() {
    const userId =
      sessionStorage.getItem("userId");

    setError(null);

    try {
      const quiz = await startQuiz(
        mapId,
        userId === null
          ? null
          : Number(userId)
      );

      navigate("/quiz", {
        state: {
          sessionId: quiz.sessionId,
          mapId: quiz.mapId,
          initialTarget: quiz.currentTarget,
        },
      });
    } catch (err) {
      console.error(err);

      setError("Unable to start the quiz.");
    }
  }

  /**
   * Sends the clicked polygon's region ID to the backend.
   *
   * Updates feedback, progress, correction state and score
   * using the response returned by the gameplay API.
   */
  async function handleGuess(regionId: string) {
    if (
      sending ||
      lastResult?.quizComplete
    ) {
      return;
    }

    const correcting =
      lastResult?.correctionRequired ?? false;

    setSending(true);
    setError(null);

    try {
      const result = await submitGuess(
        sessionId,
        regionId
      );

      setLastResult(result);

      if (result.quizComplete) {
        setMessage("Quiz complete!");
        return;
      }

      setCurrentTarget(
        result.currentTargetRegionId
      );

      if (correcting && result.correct) {
        setMessage(
          "Correction accepted. Moving to the next target."
        );
      } else if (result.correct) {
        setMessage("Correct!");
      } else if (result.correctionRequired) {
        setMessage("Incorrect. Select the requested region to continue."
        );
      }
    } catch (err) {
      console.error(err);

      setError("Unable to submit the guess.");
    } finally {
      setSending(false);
    }
  }

  return (
    <div
      style={{
        padding: "2rem",
        textAlign: "center",
      }}
    >
      <h1>Core Quiz Gameplay</h1>

      <p>Map: {mapId}</p>

      {!lastResult?.quizComplete && (
        <>
          <p>
            Click the region you think is correct.
          </p>

          {/* Display the selected map and its clickable polygons. */}
          <QuizMap
	    key={mapId}
            mapId={mapId}
	    targetRegionId={currentTarget}
            disabled={sending}
            onRegionClick={(regionId) => {
              void handleGuess(regionId);
            }}
          />
        </>
      )}

      {message && (
        <h2
          aria-live="polite"
          style={{
            marginTop: "2rem",
          }}
        >
          {message}
        </h2>
      )}

      {lastResult && (
        <div>
          <p>
            Correct answers:{" "}
            {lastResult.correctAnswers}
          </p>

          <p>
            Answered targets:{" "}
            {lastResult.answeredTargets}
          </p>

          <p>
            Accuracy:{" "}
            {lastResult.accuracy.toFixed(1)}%
          </p>

          {lastResult.quizComplete && (
            <>
              {sessionStorage.getItem("userId") !== null && (
                <GameResult sessionId={sessionId} />
              )}

              <button onClick={playAgain}>
                Play again
              </button>

              <button
                onClick={() =>
                  navigate("/mapselector")
                }
              >
                Choose another map
              </button>
            </>
          )}
        </div>
      )}

      {sending && (
        <p>Submitting...</p>
      )}

      {error && (
        <p role="alert">{error}</p>
      )}
    </div>
  );
}

/**
 * Remounts the quiz when the navigation entry changes
 * so that a new session starts with fresh component state.
 */
function QuizPage() {
  const location = useLocation();

  return <Quiz key={location.key} />;
}

export default QuizPage;
