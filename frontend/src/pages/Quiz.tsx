// Plays a quiz, then shows the result and offers another go.
import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import {
  startQuiz,
  submitGuess,
  type GuessResponse,
} from "../services/gameplayAPI";
import GameResult from "../components/GameResult";

interface QuizLocationState {
  sessionId: string;
  mapId: string;
  initialTarget: string;
}

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
          onClick={() =>
            navigate("/mapselector")
          }
        >
          Choose a map
        </button>
      </div>
    );
  }

  const sessionId = quizState.sessionId;
  const mapId = quizState.mapId;

  async function playAgain() {
    const userId =
      sessionStorage.getItem("userId");

    try {
      const quiz =
        await startQuiz(
          mapId,
          userId === null
            ? null
            : Number(userId)
        );

      navigate("/quiz", {
        state: {
          sessionId:
            quiz.sessionId,

          mapId:
            quiz.mapId,

          initialTarget:
            quiz.currentTarget,
        },
      });

    } catch (err) {
      console.error(err);

      setError(
        "Unable to start the quiz."
      );
    }
  }

  async function handleGuess(
    regionId: string
  ) {
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
      const result =
        await submitGuess(
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
      } else if (
        result.correctionRequired
      ) {
        setMessage(
          `Incorrect. Click region ${result.targetRegionId} to continue.`
        );
      }

    } catch (err) {
      console.error(err);

      setError(
        "Unable to submit the guess."
      );

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

      <p>
        Map: {mapId}
      </p>

      {!lastResult?.quizComplete && (
        <>
          <h2>
            Find region: {currentTarget}
          </h2>

          <p>
            Click the region you think is correct.
          </p>

          <div
            style={{
              display: "flex",
              justifyContent: "center",
              gap: "1rem",
              marginTop: "2rem",
            }}
          >
            <button
              disabled={sending}
              onClick={() =>
                handleGuess("1")
              }
            >
              Region 1
            </button>

            <button
              disabled={sending}
              onClick={() =>
                handleGuess("2")
              }
            >
              Region 2
            </button>

            <button
              disabled={sending}
              onClick={() =>
                handleGuess("3")
              }
            >
              Region 3
            </button>
          </div>
        </>
      )}

      {message && (
        <h2
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
        <p>{error}</p>
      )}
    </div>
  );
}

function QuizPage() {
  const location = useLocation();

  return <Quiz key={location.key} />;
}

export default QuizPage;